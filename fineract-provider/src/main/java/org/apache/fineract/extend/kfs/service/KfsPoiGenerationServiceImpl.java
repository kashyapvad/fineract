/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.extend.kfs.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.extend.kfs.dto.KfsDocumentData;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationRequest;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationResult;
import org.apache.fineract.extend.kfs.dto.RepaymentScheduleData;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblCellMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.springframework.stereotype.Service;

/**
 * POI-based KFS document generation service implementation.
 *
 * This implementation mirrors the docx4j structure but uses Apache POI instead, providing: - No JAXB version conflicts
 * - Simpler API for complex table creation - Better performance and maintenance - Uses dependencies already available
 * in Fineract - Integrates with existing data mapping services - Includes all required sections: Part 1, Part 2, Annex
 * B, and Annex C
 */
@Slf4j
@Service("poiKfsDocxGenerationService")
@RequiredArgsConstructor
public class KfsPoiGenerationServiceImpl implements KfsDocxGenerationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Inject the same data mapping services used by docx4j
    private final KfsDataMappingService kfsDataMappingService;
    private final KfsRbiCompliantMappingServiceImpl rbiCompliantMappingService;

    @Override
    public KfsDocumentGenerationResult generateKfsDocument(KfsDocumentGenerationRequest request) {
        log.info("Generating RBI-compliant KFS document using POI for loan ID: {}", request.getLoanId());

        try {
            // Extract loan data from database (same as docx4j)
            KfsDocumentData documentData = kfsDataMappingService.mapLoanDataToKfsFormat(request);
            log.info("Successfully mapped data for loan: {} - Client: {}", documentData.getLoanAccountNumber(),
                    documentData.getClientName());

            // Generate DOCX document using POI
            byte[] documentBytes = generateRbiCompliantDocxWithPoi(documentData);

            // Create successful result
            KfsDocumentGenerationResult result = new KfsDocumentGenerationResult();
            result.setStatus("SUCCESS");
            result.setMessage("KFS document generated successfully using Apache POI");
            result.setDocumentContent(documentBytes);
            result.setGenerationDate(LocalDate.now(ZoneId.systemDefault()));
            result.setFileSize((long) documentBytes.length);

            log.info("Successfully generated KFS document using POI for loan ID: {} (Size: {} bytes)", request.getLoanId(),
                    documentBytes.length);
            return result;

        } catch (Exception e) {
            log.error("Error generating KFS document using POI for loan ID: {}", request.getLoanId(), e);

            KfsDocumentGenerationResult result = new KfsDocumentGenerationResult();
            result.setStatus("FAILED");
            result.setMessage("Document generation failed: " + e.getMessage());
            result.setGenerationDate(LocalDate.now(ZoneId.systemDefault()));

            return result;
        }
    }

    @Override
    public KfsDocumentGenerationResult previewKfsDocument(KfsDocumentGenerationRequest request) {
        log.info("Generating KFS document preview using POI for loan ID: {}", request.getLoanId());
        request.setPreview(true);
        return generateKfsDocument(request);
    }

    /**
     * Generates RBI-compliant DOCX document using Apache POI with actual database data.
     */
    private byte[] generateRbiCompliantDocxWithPoi(KfsDocumentData documentData) throws Exception {
        log.info("Creating RBI-compliant DOCX structure using POI for loan: {}", documentData.getLoanAccountNumber());

        try (XWPFDocument document = new XWPFDocument()) {

            // Create field mapping using the same service as docx4j
            Map<String, Object> fieldMapping = rbiCompliantMappingService.createRbiCompliantFieldMapping(documentData);
            log.info("Created field mapping with {} entries", fieldMapping.size());

            // Create complete KFS document structure (same sections as docx4j)
            createKfsDocumentStructureWithPoi(document, fieldMapping, documentData);
            log.info("KFS document structure created successfully using POI");

            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            byte[] result = outputStream.toByteArray();

            log.info("Document converted to byte array successfully, size: {} bytes", result.length);
            return result;

        } catch (Exception e) {
            log.error("Error in generateRbiCompliantDocxWithPoi", e);
            throw new RuntimeException("POI-based document generation failed", e);
        }
    }

    /**
     * Creates the complete RBI KFS document structure using POI (mirrors docx4j structure).
     */
    private void createKfsDocumentStructureWithPoi(XWPFDocument document, Map<String, Object> fieldMapping, KfsDocumentData documentData) {

        // Add document header
        addDocumentHeaderWithPoi(document, fieldMapping);

        // Add Part 1: Interest rate and fees/charges (same as docx4j)
        addPart1InterestRatesAndFeesWithPoi(document, fieldMapping, documentData);

        // Add Part 2: Other qualitative information (missing in original POI implementation)
        addPart2QualitativeInformationWithPoi(document, fieldMapping);

        // Add Annex B: APR computation illustration (missing in original POI implementation)
        addAnnexBComputationIllustrationWithPoi(document, fieldMapping);

        // Add Annex C: Repayment schedule with actual data
        addRepaymentScheduleTableWithPoi(document, documentData);

        log.info("Complete RBI KFS document structure created successfully using POI");
    }

    /**
     * Adds document header with title and basic information using POI.
     */
    private void addDocumentHeaderWithPoi(XWPFDocument document, Map<String, Object> fieldMapping) {
        // Add title paragraphs (same structure as docx4j)
        createCenteredParagraph(document, "Annex A");
        createCenteredParagraph(document, "Key Facts Statement");
        addEmptyParagraph(document);

        // Add basic loan information using actual data
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("Part 1 (Interest rate and fees/charges) - Loan ID: " + getString(fieldMapping, "LOAN_PROPOSAL_ACCOUNT_NO"));
        run.setBold(true);
        addEmptyParagraph(document);
    }

    /**
     * Creates Part 1 table structure using POI with actual database data (mirrors docx4j implementation).
     */
    private void addPart1InterestRatesAndFeesWithPoi(XWPFDocument document, Map<String, Object> fieldMapping,
            KfsDocumentData documentData) {
        log.info("Creating Part 1 table using POI for Interest rates and fees");

        // Create table with 3 columns
        XWPFTable table = document.createTable(1, 3);
        setTableProperties(table);
        setColumnWidths(table, 600, 4000, 4400);

        // Add table rows using actual data from field mapping (same as docx4j)
        addTableRowPoi(table, "1", "Loan proposal/ account No.", getString(fieldMapping, "LOAN_PROPOSAL_ACCOUNT_NO"));
        addTableRowPoi(table, "2", "Type of Loan", getString(fieldMapping, "TYPE_OF_LOAN"));
        addTableRowPoi(table, "3", "Sanctioned Loan amount (in Rupees)", getString(fieldMapping, "SANCTIONED_LOAN_AMOUNT"));

        // Disbursal schedule section
        addTableRowPoi(table, "4", "Disbursal schedule", "");
        addTableRowPoi(table, "", "(i) Disbursement in stages or 100% upfront.", getString(fieldMapping, "DISBURSEMENT_TYPE"));
        addTableRowPoi(table, "", "(ii) If it is stage wise, mention the clause of loan agreement having relevant details",
                getString(fieldMapping, "STAGE_WISE_DETAILS"));

        addTableRowPoi(table, "5", "Loan term (year/months/days)", getString(fieldMapping, "LOAN_TERM"));

        // Add complex instalment details row using actual data
        addInstallmentDetailsRowPoi(table, fieldMapping);

        addTableRowPoi(table, "6", "Interest rate (%) and type (fixed or floating or hybrid)",
                formatCombinedValue(getString(fieldMapping, "INTEREST_RATE"), getString(fieldMapping, "INTEREST_RATE_TYPE"), " (", ")"));
        addTableRowPoi(table, "7", "Additional Information in case of Floating rate of interest",
                getString(fieldMapping, "FLOATING_RATE_INFO"));

        // Add Fee/Charges row using actual data
        addFeeChargesRowPoi(table, fieldMapping);

        // Add remaining rows with actual data
        addTableRowPoi(table, "9", "Annual Percentage Rate (APR) (%)", getString(fieldMapping, "APR"));

        // Contingent charges section
        addTableRowPoi(table, "10", "Details of Contingent Charges (in ₹ or %, as applicable)", "");
        addTableRowPoi(table, "", "(i) Penal charges, if any, in case of delayed payment",
                getString(fieldMapping, "PENAL_CHARGES_DELAYED"));
        addTableRowPoi(table, "", "(ii) Other penal charges, if any", getString(fieldMapping, "OTHER_PENAL_CHARGES"));
        addTableRowPoi(table, "", "(iii) Foreclosure charges, if applicable", getString(fieldMapping, "FORECLOSURE_CHARGES"));
        addTableRowPoi(table, "", "(iv) Charges for switching of loans from floating to fixed rate and vice versa",
                getString(fieldMapping, "SWITCHING_CHARGES"));
        addTableRowPoi(table, "", "(v) Any other charges (please specify)", getString(fieldMapping, "OTHER_CHARGES"));

        addEmptyParagraph(document);
    }

    /**
     * Adds Part 2: Other qualitative information using POI (mirrors docx4j implementation).
     */
    private void addPart2QualitativeInformationWithPoi(XWPFDocument document, Map<String, Object> fieldMapping) {
        log.info("Creating Part 2 table using POI for qualitative information with 3-column structure (matching Annex B)");

        // Add Part 2 header
        addEmptyParagraph(document);
        XWPFParagraph part2Header = document.createParagraph();
        XWPFRun headerRun = part2Header.createRun();
        headerRun.setText("Part 2 (Other qualitative information)");
        headerRun.setBold(true);
        addEmptyParagraph(document);

        // Create Part 2 table with 3-column structure (matching Annex B format exactly)
        XWPFTable table = document.createTable(1, 3);
        setTableProperties(table);
        setColumnWidths(table, 800, 6000, 2200); // Same proportions as Annex B

        // Add table header row (matching Annex B structure)
        addTableRowPoi(table, "Sr. No.", "Parameter", "Details");

        // Add Part 2 rows using 3-column format with actual data (following DRY principle)
        addTableRowPoi(table, "1", "Clause of Loan agreement relating to engagement of recovery agents",
                getString(fieldMapping, "RECOVERY_AGENTS_CLAUSE"));

        addTableRowPoi(table, "2", "Clause of Loan agreement which details grievance redressal mechanism",
                getString(fieldMapping, "GRIEVANCE_REDRESSAL_CLAUSE"));

        addTableRowPoi(table, "3", "Phone number and email id of the nodal grievance redressal officer",
                formatCombinedValue(getString(fieldMapping, "NODAL_OFFICER_PHONE"), getString(fieldMapping, "NODAL_OFFICER_EMAIL"), " / "));

        addTableRowPoi(table, "4", "Whether the loan is, or in future maybe, subject to transfer to other REs or securitisation (Yes/ No)",
                getString(fieldMapping, "TRANSFER_SECURITISATION"));

        // Collaborative lending section with 3-column structure
        addTableRowPoi(table, "5",
                "In case of lending under collaborative lending arrangements (e.g., co-lending/ outsourcing), following additional details may be furnished:",
                "");

        // Add collaborative lending subsections (using 3-column format)
        addPart2CollaborativeLendingTable(table, fieldMapping);

        // Digital loans section with 3-column structure
        addTableRowPoi(table, "6", "In case of digital loans, following specific disclosures may be furnished:", "");
        addTableRowPoi(table, "",
                "(i) Cooling off/look-up period, in terms of RE&apos;s board approved policy, during which borrower shall not be charged any penalty on prepayment of loan",
                getString(fieldMapping, "COOLING_OFF_PERIOD"));
        addTableRowPoi(table, "", "(ii) Details of LSP acting as recovery agent and authorized to approach the borrower",
                getString(fieldMapping, "LSP_RECOVERY_AGENT"));

        addEmptyParagraph(document);
    }

    /**
     * Adds Annex B: Illustration for computation of APR using POI (mirrors docx4j implementation).
     */
    private void addAnnexBComputationIllustrationWithPoi(XWPFDocument document, Map<String, Object> fieldMapping) {
        log.info("Creating Annex B table using POI for APR computation illustration");

        // Add page break and Annex B header
        addEmptyParagraph(document);
        addEmptyParagraph(document);
        createCenteredParagraph(document, "Annex B");
        createCenteredParagraph(document, "Illustration for computation of APR for Retail and MSME loans");
        addEmptyParagraph(document);

        // Create Annex B table with 3 columns
        XWPFTable table = document.createTable(1, 3);
        setTableProperties(table);
        setColumnWidths(table, 800, 6000, 2200);

        // Add table header row
        addTableRowPoi(table, "Sr. No.", "Parameter", "Details");

        // Add Annex B rows using actual data (same structure as docx4j)
        addTableRowPoi(table, "1", "Sanctioned Loan amount (in Rupees) (SI no. 2 of the KFS template – Part 1)",
                getString(fieldMapping, "ANNEX_B_SANCTIONED_AMOUNT"));

        // Loan Term section with subsections
        addTableRowPoi(table, "2", "Loan Term (in years/ months/ days) (SI No.4 of the KFS template – Part 1)", "");
        addTableRowPoi(table, "", "(i) Loan Term", getString(fieldMapping, "ANNEX_B_LOAN_TERM_TEXT"));
        addTableRowPoi(table, "", "(ii) Total number of installments", getString(fieldMapping, "ANNEX_B_NUMBER_OF_EPIS"));
        addTableRowPoi(table, "", "(iii) Type of repayment", getString(fieldMapping, "ANNEX_B_TYPE_OF_EPI"));

        addTableRowPoi(table, "a)", "No. of instalments for payment of principal, in case of non-equated periodic loans",
                getString(fieldMapping, "ANNEX_B_NO_INSTALMENTS_NON_EQUATED"));

        // Complex row for Type of EPI
        addAnnexBComplexRowPoi(table, "b)",
                "Type of EPI\nAmount of each EPI (in Rupees) and\nnos. of EPIs (e.g., no. of EMIs in case of monthly instalments)\n(SI No. 5 of the KFS template – Part 1)",
                formatMultiLineValue(getString(fieldMapping, "ANNEX_B_TYPE_OF_EPI"), getString(fieldMapping, "ANNEX_B_EPI_AMOUNT"),
                        getString(fieldMapping, "ANNEX_B_NUMBER_OF_EPIS")));

        addTableRowPoi(table, "c)", "No. of instalments for payment of capitalised interest, if any",
                getString(fieldMapping, "ANNEX_B_NO_INSTALMENTS_CAPITALISED"));

        addTableRowPoi(table, "d)", "Commencement of repayments, post sanction (SI No. 5 of the KFS template – Part 1)",
                getString(fieldMapping, "ANNEX_B_COMMENCEMENT_REPAYMENT"));

        // Continue with remaining Annex B rows
        addTableRowPoi(table, "3", "Interest rate type (fixed or floating or hybrid) (SI No. 6 of the KFS template – Part 1)",
                getString(fieldMapping, "ANNEX_B_INTEREST_RATE_TYPE"));

        addTableRowPoi(table, "4", "Rate of Interest (SI No. 6 of the KFS template – Part 1)",
                getString(fieldMapping, "ANNEX_B_RATE_OF_INTEREST"));

        addTableRowPoi(table, "5",
                "Total Interest Amount to be charged during the entire tenor of the loan as per the rate prevailing on sanction date (in Rupees)",
                getString(fieldMapping, "ANNEX_B_TOTAL_INTEREST_AMOUNT"));

        addTableRowPoi(table, "6", "Fee/ Charges payable (in Rupees)", getString(fieldMapping, "ANNEX_B_FEE_CHARGES_PAYABLE"));

        addTableRowPoi(table, "A", "Payable to the RE (SI No.8A of the KFS template-Part 1)",
                getString(fieldMapping, "ANNEX_B_PAYABLE_TO_RE"));

        addTableRowPoi(table, "B", "Payable to third-party routed through RE (SI No 8B of the KFS template – Part 1)",
                getString(fieldMapping, "ANNEX_B_PAYABLE_TO_THIRD_PARTY"));

        addTableRowPoi(table, "7", "Net disbursed amount (1-6) (in Rupees)", getString(fieldMapping, "ANNEX_B_NET_DISBURSED_AMOUNT"));

        addTableRowPoi(table, "8", "Total amount to be paid by the borrower (sum of 1 and 5) (in Rupees)",
                getString(fieldMapping, "ANNEX_B_TOTAL_AMOUNT_PAYABLE"));

        addTableRowPoi(table, "9",
                "Annual Percentage rate- Effective annualized interest rate (in percentage)¹⁰ (SI No.9 of the KFS template-Part 1)",
                getString(fieldMapping, "ANNEX_B_APR_PERCENTAGE"));

        addTableRowPoi(table, "10", "Schedule of disbursement as per terms and conditions",
                getString(fieldMapping, "ANNEX_B_DISBURSEMENT_SCHEDULE"));

        addTableRowPoi(table, "11", "Due date of payment of instalment and interest", getString(fieldMapping, "ANNEX_B_DUE_DATE"));

        // Add footnote
        addEmptyParagraph(document);
        XWPFParagraph footnote = document.createParagraph();
        footnote.createRun().setText(
                "**The points 5,8,9 and 11 mentioned above against the columns is tentative and may be vary as per the actual date of disbursement of the loan.");

        addEmptyParagraph(document);
    }

    /**
     * Adds repayment schedule table using POI with actual loan data.
     */
    private void addRepaymentScheduleTableWithPoi(XWPFDocument document, KfsDocumentData documentData) {
        log.info("Creating repayment schedule table using POI with actual data");

        // Add section header
        createCenteredParagraph(document, "Annex C");
        createCenteredParagraph(document, "Illustrative Repayment Schedule under Equated Periodic Instalment");
        addEmptyParagraph(document);

        // Create repayment schedule table
        XWPFTable table = document.createTable(1, 6);
        setTableProperties(table);

        // Add header row
        XWPFTableRow headerRow = table.getRow(0);
        headerRow.getCell(0).setText("Instalment No.");
        headerRow.getCell(1).setText("Outstanding Principal (in Rupees)");
        headerRow.getCell(2).setText("Principal (in Rupees)");
        headerRow.getCell(3).setText("Interest (in Rupees)");
        headerRow.getCell(4).setText("Instalment (in Rupees)");
        headerRow.getCell(5).setText("Due Date");

        // Make header bold
        for (XWPFTableCell cell : headerRow.getTableCells()) {
            if (!cell.getParagraphs().isEmpty() && !cell.getParagraphs().get(0).getRuns().isEmpty()) {
                cell.getParagraphs().get(0).getRuns().get(0).setBold(true);
            }
        }

        // Add actual repayment schedule data
        List<RepaymentScheduleData> scheduleData = documentData.getRepaymentSchedule();
        if (scheduleData != null && !scheduleData.isEmpty()) {
            for (RepaymentScheduleData schedule : scheduleData) {
                XWPFTableRow dataRow = table.createRow();
                dataRow.getCell(0).setText(schedule.getInstallmentNumber() != null ? schedule.getInstallmentNumber().toString() : "");
                dataRow.getCell(1).setText(formatAmount(schedule.getOutstandingBalance()));
                dataRow.getCell(2).setText(formatAmount(schedule.getPrincipalAmount()));
                dataRow.getCell(3).setText(formatAmount(schedule.getInterestAmount()));
                dataRow.getCell(4).setText(formatAmount(schedule.getTotalAmount()));
                dataRow.getCell(5).setText(schedule.getDueDate() != null ? schedule.getDueDate().format(DATE_FORMATTER) : "");
            }
        } else {
            // Fallback: Add a note if no schedule data available
            XWPFTableRow dataRow = table.createRow();
            dataRow.getCell(0).setText("N/A");
            dataRow.getCell(1).setText("Schedule data not available");
            dataRow.getCell(2).setText("");
            dataRow.getCell(3).setText("");
            dataRow.getCell(4).setText("");
            dataRow.getCell(5).setText("");
        }

        addEmptyParagraph(document);
    }

    private void addPart2CollaborativeLendingTable(XWPFTable parentTable, Map<String, Object> fieldMapping) {
        // Add collaborative lending subsections using 3-column format (matching Annex B structure exactly)
        // This follows the same pattern as other sections and avoids duplicate N/A values

        // Add the three subsection rows with proper data combination (following DRY principle)
        addTableRowPoi(parentTable, "", "(i) Name of the originating RE, along with its funding proportion",
                formatCollaborativeLendingValue(getString(fieldMapping, "COLLABORATIVE_LENDING_ORIGINATING_RE_NAME"),
                        getString(fieldMapping, "COLLABORATIVE_LENDING_ORIGINATING_RE_FUNDING")));

        addTableRowPoi(parentTable, "", "(ii) Name of the partner RE along with its proportion of funding",
                formatCollaborativeLendingValue(getString(fieldMapping, "COLLABORATIVE_LENDING_PARTNER_RE_NAME"),
                        getString(fieldMapping, "COLLABORATIVE_LENDING_PARTNER_RE_FUNDING")));

        addTableRowPoi(parentTable, "", "(iii) Blended rate of interest", getString(fieldMapping, "COLLABORATIVE_LENDING_BLENDED_RATE"));
    }

    /**
     * Formats collaborative lending values to avoid duplicate N/A and follow proper formatting. This helper method
     * ensures consistent data presentation and follows DRY principle.
     */
    private String formatCollaborativeLendingValue(String name, String funding) {
        return formatCombinedValue(name, funding, " (", ")");
    }

    /**
     * Generic helper method to format combined values with custom separators, avoiding duplicate N/A. This follows DRY
     * principle and ensures consistent data presentation across all sections.
     */
    private String formatCombinedValue(String value1, String value2, String separator) {
        return formatCombinedValue(value1, value2, separator, "");
    }

    /**
     * Generic helper method to format combined values with custom separators and suffix, avoiding duplicate N/A. This
     * follows DRY principle and ensures consistent data presentation across all sections.
     */
    private String formatCombinedValue(String value1, String value2, String separator, String suffix) {
        // Check if values are N/A or empty
        boolean isValue1NA = isValueNA(value1);
        boolean isValue2NA = isValueNA(value2);

        // If both are N/A, return single N/A
        if (isValue1NA && isValue2NA) {
            return "N/A";
        }

        // If only first value is available
        if (isValue2NA) {
            return value1;
        }

        // If only second value is available
        if (isValue1NA) {
            return value2;
        }

        // If both have actual values, combine them
        return value1 + separator + value2 + suffix;
    }

    /**
     * Formats multiple values with line breaks, avoiding duplicate N/A values. This ensures clean multi-line formatting
     * for complex data fields.
     */
    private String formatMultiLineValue(String... values) {
        if (values == null || values.length == 0) {
            return "N/A";
        }

        // Filter out N/A values
        List<String> validValues = new ArrayList<>();
        for (String value : values) {
            if (!isValueNA(value)) {
                validValues.add(value);
            }
        }

        // If no valid values, return single N/A
        if (validValues.isEmpty()) {
            return "N/A";
        }

        // Join valid values with double line breaks
        return String.join("\n\n", validValues);
    }

    /**
     * Helper method to check if a value is considered N/A or empty. Centralizes the N/A checking logic following DRY
     * principle.
     */
    private boolean isValueNA(String value) {
        return value == null || value.trim().isEmpty() || "N/A".equalsIgnoreCase(value.trim());
    }

    // Helper methods for table creation and formatting

    private void setTableProperties(XWPFTable table) {
        CTTbl ctTbl = table.getCTTbl();
        CTTblPr tblPr = ctTbl.getTblPr() != null ? ctTbl.getTblPr() : ctTbl.addNewTblPr();

        // Set table width to 100%
        CTTblWidth tblWidth = tblPr.getTblW() != null ? tblPr.getTblW() : tblPr.addNewTblW();
        tblWidth.setType(STTblWidth.PCT);
        tblWidth.setW(BigDecimal.valueOf(5000));

        // Add borders with better spacing
        CTTblBorders borders = tblPr.getTblBorders() != null ? tblPr.getTblBorders() : tblPr.addNewTblBorders();
        borders.addNewTop().setVal(STBorder.SINGLE);
        borders.addNewBottom().setVal(STBorder.SINGLE);
        borders.addNewLeft().setVal(STBorder.SINGLE);
        borders.addNewRight().setVal(STBorder.SINGLE);
        borders.addNewInsideH().setVal(STBorder.SINGLE);
        borders.addNewInsideV().setVal(STBorder.SINGLE);

        // Add table cell margins for better spacing (improved spacing like image 2)
        CTTblCellMar cellMar = tblPr.getTblCellMar() != null ? tblPr.getTblCellMar() : tblPr.addNewTblCellMar();

        // Set cell margins (in twentieths of a point - 1440 = 1 inch)
        // Increased margins for better spacing and readability
        if (cellMar.getTop() == null) {
            CTTblWidth topMargin = cellMar.addNewTop();
            topMargin.setW(BigInteger.valueOf(180)); // 0.125 inch top margin (increased)
            topMargin.setType(STTblWidth.DXA);
        }

        if (cellMar.getBottom() == null) {
            CTTblWidth bottomMargin = cellMar.addNewBottom();
            bottomMargin.setW(BigInteger.valueOf(180)); // 0.125 inch bottom margin (increased)
            bottomMargin.setType(STTblWidth.DXA);
        }

        if (cellMar.getLeft() == null) {
            CTTblWidth leftMargin = cellMar.addNewLeft();
            leftMargin.setW(BigInteger.valueOf(144)); // 0.1 inch left margin (increased)
            leftMargin.setType(STTblWidth.DXA);
        }

        if (cellMar.getRight() == null) {
            CTTblWidth rightMargin = cellMar.addNewRight();
            rightMargin.setW(BigInteger.valueOf(144)); // 0.1 inch right margin (increased)
            rightMargin.setType(STTblWidth.DXA);
        }
    }

    private void setColumnWidths(XWPFTable table, int... widths) {
        for (XWPFTableRow row : table.getRows()) {
            List<XWPFTableCell> cells = row.getTableCells();
            for (int i = 0; i < cells.size() && i < widths.length; i++) {
                cells.get(i).setWidth(String.valueOf(widths[i]));
            }
        }
    }

    private void addTableRowPoi(XWPFTable table, String col1Text, String col2Text, String col3Text) {
        XWPFTableRow row = table.createRow();
        row.getCell(0).setText(sanitizeText(col1Text));
        row.getCell(1).setText(sanitizeText(col2Text));
        row.getCell(2).setText(sanitizeText(col3Text));
    }

    private void addInstallmentDetailsRowPoi(XWPFTable table, Map<String, Object> fieldMapping) {
        XWPFTableRow row = table.createRow();

        row.getCell(0).setText("5");

        // Second column with multiple paragraphs
        XWPFTableCell cell2 = row.getCell(1);
        cell2.removeParagraph(0);
        cell2.addParagraph().createRun().setText("Instalment details");
        cell2.addParagraph().createRun().setText("Type of instalments: " + getString(fieldMapping, "TYPE_OF_INSTALMENTS"));
        cell2.addParagraph().createRun().setText("Number of EPIs: " + getString(fieldMapping, "NUMBER_OF_EPIS"));

        // Third column with multiple paragraphs
        XWPFTableCell cell3 = row.getCell(2);
        cell3.removeParagraph(0);
        cell3.addParagraph().createRun().setText("EPI (₹): " + getString(fieldMapping, "EPI_AMOUNT"));
        cell3.addParagraph().createRun()
                .setText("Commencement of repayment, post sanction: " + getString(fieldMapping, "COMMENCEMENT_OF_REPAYMENT"));
    }

    private void addFeeChargesRowPoi(XWPFTable table, Map<String, Object> fieldMapping) {
        XWPFTableRow row = table.createRow();

        row.getCell(0).setText("8");
        row.getCell(1).setText("Fee/ Charges");

        // Third column with nested structure
        XWPFTableCell cell3 = row.getCell(2);
        cell3.removeParagraph(0);

        // Payable to RE section
        cell3.addParagraph().createRun().setText("Payable to the RE (A)");
        cell3.addParagraph().createRun().setText("(i) Processing fees: " + getString(fieldMapping, "PROCESSING_FEES_RE"));
        cell3.addParagraph().createRun().setText("(ii) Insurance charges: " + getString(fieldMapping, "INSURANCE_CHARGES_RE"));
        cell3.addParagraph().createRun().setText("(iii) Valuation fees: " + getString(fieldMapping, "VALUATION_FEES_RE"));
        cell3.addParagraph().createRun().setText("(iv) Any other: " + getString(fieldMapping, "OTHER_FEES_RE"));
        cell3.addParagraph().createRun().setText("");

        // Payable to third party section
        cell3.addParagraph().createRun().setText("Payable to a third party through RE (B)");
        cell3.addParagraph().createRun().setText("(i) Processing fees: " + getString(fieldMapping, "PROCESSING_FEES_THIRD_PARTY"));
        cell3.addParagraph().createRun().setText("(ii) Insurance charges: " + getString(fieldMapping, "INSURANCE_CHARGES_THIRD_PARTY"));
        cell3.addParagraph().createRun().setText("(iii) Valuation fees: " + getString(fieldMapping, "VALUATION_FEES_THIRD_PARTY"));
        cell3.addParagraph().createRun().setText("(iv) Any other: " + getString(fieldMapping, "OTHER_FEES_THIRD_PARTY"));
    }

    private void addAnnexBComplexRowPoi(XWPFTable table, String col1Text, String col2Text, String col3Text) {
        XWPFTableRow row = table.createRow();

        row.getCell(0).setText(sanitizeText(col1Text));

        // Handle multi-line content in column 2
        XWPFTableCell cell2 = row.getCell(1);
        cell2.removeParagraph(0);
        String[] col2Lines = splitLines(col2Text);
        for (String line : col2Lines) {
            if (!line.trim().isEmpty()) {
                cell2.addParagraph().createRun().setText(sanitizeText(line));
            }
        }
        if (cell2.getParagraphs().isEmpty()) {
            cell2.addParagraph().createRun().setText("");
        }

        // Handle multi-line content in column 3
        XWPFTableCell cell3 = row.getCell(2);
        cell3.removeParagraph(0);
        String[] col3Lines = splitLines(col3Text);
        for (String line : col3Lines) {
            if (!line.trim().isEmpty()) {
                cell3.addParagraph().createRun().setText(sanitizeText(line));
            }
        }
        if (cell3.getParagraphs().isEmpty()) {
            cell3.addParagraph().createRun().setText("");
        }
    }

    // Utility methods

    /**
     * Helper method to split text by newlines following DRY principle. Centralizes line splitting logic to avoid code
     * duplication.
     */
    private String[] splitLines(String text) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }
        return text.split("\\n");
    }

    private void createCenteredParagraph(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(true);
    }

    private void addEmptyParagraph(XWPFDocument document) {
        document.createParagraph().createRun().setText("");
    }

    private String sanitizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
    }

    private String getString(Map<String, Object> fieldMapping, String key) {
        Object value = fieldMapping.get(key);
        if (value == null) {
            return "N/A";
        }
        return sanitizeText(value.toString());
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "₹ 0.00";
        }
        return String.format("₹ %,.2f", amount.doubleValue());
    }

    @Override
    public String getServiceName() {
        return "poi";
    }

    @Override
    public boolean isAvailable() {
        return true; // POI is always available as it's part of core dependencies
    }
}
