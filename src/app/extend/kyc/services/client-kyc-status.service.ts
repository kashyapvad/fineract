import { Injectable } from '@angular/core';
import { Observable, of, BehaviorSubject, EMPTY, timer } from 'rxjs';
import { map, catchError, shareReplay, tap, concatMap, delay, debounceTime, switchMap } from 'rxjs/operators';
import { ClientKycService } from './client-kyc.service';

export interface KycStatusInfo {
  isVerified: boolean;
  verifiedDocumentCount: number;
  totalRequiredDocuments: number;
  hasRequiredDocuments: boolean; // PAN and Aadhaar both verified
  lastVerifiedOn?: Date; // Keep as raw data for dateFormat pipe
}

@Injectable({
  providedIn: 'root'
})
export class ClientKycStatusService {
  private batchCache = new Map<number, KycStatusInfo>();
  private cacheTimestamps = new Map<number, number>();
  private pendingRequests = new Set<number>();
  private readonly cacheTimeout = 30000; // 30 seconds

  // Batch loading observables
  private batchLoadSubject = new BehaviorSubject<Map<number, KycStatusInfo>>(new Map());
  public batchLoadStatus$ = this.batchLoadSubject.asObservable();

  constructor(private kycService: ClientKycService) {}

  /**
   * Clear all caches - useful for testing or when data needs refresh
   */
  clearCache(): void {
    this.batchCache.clear();
    this.cacheTimestamps.clear();
    this.pendingRequests.clear();
  }

  /**
   * Get KYC status for multiple clients efficiently using batch processing
   * This is the preferred method for table/list components
   */
  getKycStatusBatch(clientIds: number[]): Observable<Map<number, KycStatusInfo>> {
    if (clientIds.length === 0) {
      return of(new Map());
    }

    const now = Date.now();
    const resultMap = new Map<number, KycStatusInfo>();
    const uncachedClientIds: number[] = [];

    // Check cache first
    clientIds.forEach(clientId => {
      const cached = this.batchCache.get(clientId);
      const lastCached = this.cacheTimestamps.get(clientId);
      
      if (cached && lastCached && (now - lastCached) < this.cacheTimeout) {
        resultMap.set(clientId, cached);
      } else {
        uncachedClientIds.push(clientId);
      }
    });

    if (uncachedClientIds.length === 0) {
      return of(resultMap);
    }

    // Load uncached clients
    return this.loadBatchClients(uncachedClientIds).pipe(
      map(() => {
        // Merge cached and newly loaded data
        clientIds.forEach(clientId => {
          const status = this.batchCache.get(clientId);
          if (status) {
            resultMap.set(clientId, status);
          }
        });
        return resultMap;
      })
    );
  }

  /**
   * Internal method to load multiple clients with controlled concurrency
   */
  private loadBatchClients(clientIds: number[]): Observable<void> {
    // Process clients sequentially with small delay to avoid overwhelming the server
    return of(...clientIds).pipe(
      concatMap((clientId, index) => {
        return timer(index * 50).pipe( // 50ms delay between requests
          switchMap(() => {
            return this.kycService.getKycDetails(clientId).pipe(
              map(kycData => this.processKycStatus(kycData)),
              catchError(error => {
                return of({
                  isVerified: false,
                  verifiedDocumentCount: 0,
                  totalRequiredDocuments: 2,
                  hasRequiredDocuments: false
                });
              }),
              tap(status => {
                this.batchCache.set(clientId, status);
                this.cacheTimestamps.set(clientId, Date.now());
              })
            );
          })
        );
      }),
      tap(() => {
        // Notify subscribers of batch update
        this.batchLoadSubject.next(new Map(this.batchCache));
      }),
      map(() => void 0) // Convert to void
    );
  }

  /**
   * Get KYC status for individual client - ONLY for non-batch scenarios
   * This should be avoided in table/list components
   */
  getKycStatusIndividual(clientId: number): Observable<KycStatusInfo> {
    // Check batch cache first
    if (this.batchCache.has(clientId)) {
      const now = Date.now();
      const lastCached = this.cacheTimestamps.get(clientId);
      if (lastCached && (now - lastCached) < this.cacheTimeout) {
        return of(this.batchCache.get(clientId)!);
      }
    }

    // Check if request is already pending
    if (this.pendingRequests.has(clientId)) {
      return this.batchLoadStatus$.pipe(
        map((): KycStatusInfo | undefined => this.batchCache.get(clientId)),
        map((status): KycStatusInfo => status || {
          isVerified: false,
          verifiedDocumentCount: 0,
          totalRequiredDocuments: 2,
          hasRequiredDocuments: false
        })
      );
    }

    // Mark as pending
    this.pendingRequests.add(clientId);

    // Create new individual request
    return this.kycService.getKycDetails(clientId).pipe(
      map((kycData) => this.processKycStatus(kycData)),
      catchError((error) => {
        return of({
          isVerified: false,
          verifiedDocumentCount: 0,
          totalRequiredDocuments: 2,
          hasRequiredDocuments: false
        });
      }),
      tap(status => {
        // Update cache
        this.batchCache.set(clientId, status);
        this.cacheTimestamps.set(clientId, Date.now());
        this.pendingRequests.delete(clientId);
        // Notify batch subscribers
        this.batchLoadSubject.next(new Map(this.batchCache));
      }),
      shareReplay({ bufferSize: 1, refCount: true })
    );
  }

  /**
   * Get cached status if available, otherwise return default
   */
  getCachedStatus(clientId: number): KycStatusInfo {
    const cached = this.batchCache.get(clientId);
    const now = Date.now();
    const lastCached = this.cacheTimestamps.get(clientId);
    
    if (cached && lastCached && (now - lastCached) < this.cacheTimeout) {
      return cached;
    }
    
    return {
      isVerified: false,
      verifiedDocumentCount: 0,
      totalRequiredDocuments: 2,
      hasRequiredDocuments: false
    };
  }

  /**
   * Check if client has verified KYC (PAN and Aadhaar verified)
   */
  isClientKycVerified(clientId: number): Observable<boolean> {
    return this.getKycStatusIndividual(clientId).pipe(map((status) => status.hasRequiredDocuments));
  }

  /**
   * Process KYC data to determine verification status
   */
  private processKycStatus(kycData: any): KycStatusInfo {
    if (!kycData) {
      return {
        isVerified: false,
        verifiedDocumentCount: 0,
        totalRequiredDocuments: 2,
        hasRequiredDocuments: false
      };
    }

    // Count verified documents
    const verifiedCount = this.getVerifiedDocumentCount(kycData);

    // Check if required documents (PAN and Aadhaar) are verified
    const hasRequiredDocuments = Boolean(kycData.panVerified && kycData.aadhaarVerified);

    // Parse last verified date if available
    let lastVerifiedOn: any | undefined;
    if (kycData.lastVerifiedOn) {
      if (Array.isArray(kycData.lastVerifiedOn) && kycData.lastVerifiedOn.length >= 3) {
        const [
          year,
          month,
          day
        ] = kycData.lastVerifiedOn;
        lastVerifiedOn = [
          year,
          month,
          day
        ];
      } else if (kycData.lastVerifiedOn instanceof Date) {
        lastVerifiedOn = kycData.lastVerifiedOn;
      }
    }

    return {
      isVerified: hasRequiredDocuments,
      verifiedDocumentCount: verifiedCount,
      totalRequiredDocuments: 2, // PAN and Aadhaar are required
      hasRequiredDocuments,
      lastVerifiedOn
    };
  }

  /**
   * Count verified documents
   */
  private getVerifiedDocumentCount(kycData: any): number {
    let count = 0;
    if (kycData.panVerified) count++;
    if (kycData.aadhaarVerified) count++;
    if (kycData.drivingLicenseVerified) count++;
    if (kycData.voterIdVerified) count++;
    if (kycData.passportVerified) count++;
    return count;
  }
} 