package com.formation.service;

import com.formation.entity.Grade;
import com.formation.entity.Student;
import com.formation.entity.Course;
import com.formation.repository.GradeRepository;
import com.formation.repository.StudentRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

@Service
public class ReportService {
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    /**
     * Génère un rapport PDF des notes pour un étudiant
     */
    public byte[] generateStudentGradesReport(Long studentId) throws Exception {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        
        // Calculer la moyenne
        Double average = GradeService.calculateAverage(grades);
        
        // Préparer les données pour le rapport
        List<Map<String, Object>> reportData = new ArrayList<>();
        for (Grade grade : grades) {
            Map<String, Object> row = new HashMap<>();
            row.put("courseCode", grade.getCours().getCode());
            row.put("courseTitle", grade.getCours().getTitre());
            row.put("grade", grade.getValeur());
            row.put("comment", grade.getCommentaire() != null ? grade.getCommentaire() : "");
            row.put("date", grade.getDateAttribution().toString());
            reportData.add(row);
        }
        
        // Paramètres du rapport
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("studentName", student.getNom() + " " + student.getPrenom());
        parameters.put("studentMatricule", student.getMatricule());
        parameters.put("average", average);
        parameters.put("reportDate", new Date());
        
        // Charger le template JasperReports (créer un rapport simple si le template n'existe pas)
        return generateSimplePDFReport(parameters, reportData, student.getNom() + " " + student.getPrenom());
    }
    
    /**
     * Génère un rapport PDF des notes pour un cours
     */
    public byte[] generateCourseGradesReport(Long courseId) throws Exception {
        Course course = gradeRepository.findByCoursId(courseId).stream()
            .findFirst()
            .map(Grade::getCours)
            .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
        
        List<Grade> grades = gradeRepository.findByCoursId(courseId);
        
        // Calculer le taux de réussite
        Long passing = gradeRepository.countPassingGradesByCoursId(courseId);
        Long total = gradeRepository.countTotalGradesByCoursId(courseId);
        Double successRate = total > 0 ? (passing.doubleValue() / total.doubleValue()) * 100 : 0.0;
        
        // Préparer les données
        List<Map<String, Object>> reportData = new ArrayList<>();
        for (Grade grade : grades) {
            Map<String, Object> row = new HashMap<>();
            row.put("studentName", grade.getStudent().getNom() + " " + grade.getStudent().getPrenom());
            row.put("studentMatricule", grade.getStudent().getMatricule());
            row.put("grade", grade.getValeur());
            row.put("comment", grade.getCommentaire() != null ? grade.getCommentaire() : "");
            row.put("date", grade.getDateAttribution().toString());
            reportData.add(row);
        }
        
        // Paramètres
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("courseCode", course.getCode());
        parameters.put("courseTitle", course.getTitre());
        parameters.put("successRate", successRate);
        parameters.put("totalStudents", total);
        parameters.put("passingStudents", passing);
        parameters.put("reportDate", new Date());
        
        return generateSimplePDFReport(parameters, reportData, course.getTitre());
    }
    
    /**
     * Génère un PDF simple en utilisant JasperReports avec un rapport dynamique
     */
    private byte[] generateSimplePDFReport(Map<String, Object> parameters, 
                                          List<Map<String, Object>> data,
                                          String title) throws Exception {
        // Créer une source de données à partir de la liste de maps
        // Utiliser JRMapCollectionDataSource pour les Maps
        Collection<Map<String, ?>> dataCollection = new ArrayList<>(data);
        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(dataCollection);
        
        // Créer un rapport JasperReports dynamique
        JasperReport jasperReport = createDynamicReport(title, data);
        
        // Remplir le rapport avec les données
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        
        // Exporter vers PDF
        return exportToPDF(jasperPrint);
    }
    
    /**
     * Crée un rapport JasperReports dynamique basé sur les données
     */
    private JasperReport createDynamicReport(String title, List<Map<String, Object>> data) throws JRException {
        // Si aucune donnée, créer un rapport minimal
        if (data.isEmpty()) {
            return JasperCompileManager.compileReport(
                createEmptyReportXml(title)
            );
        }
        
        // Créer un rapport dynamique basé sur les clés de la première entrée
        Map<String, Object> firstRow = data.get(0);
        return JasperCompileManager.compileReport(
            createDynamicReportXml(title, firstRow.keySet())
        );
    }
    
    /**
     * Crée un XML de rapport dynamique pour JasperReports
     */
    private InputStream createDynamicReportXml(String title, Set<String> fieldNames) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\"\n");
        xml.append("             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("             xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\"\n");
        xml.append("             name=\"dynamic_report\" pageWidth=\"595\" pageHeight=\"842\" columnWidth=\"555\"\n");
        xml.append("             leftMargin=\"20\" rightMargin=\"20\" topMargin=\"20\" bottomMargin=\"20\">\n");
        
        // Titre
        xml.append("    <title>\n");
        xml.append("        <band height=\"50\">\n");
        xml.append("            <staticText>\n");
        xml.append("                <reportElement x=\"0\" y=\"0\" width=\"555\" height=\"30\"/>\n");
        xml.append("                <textElement textAlignment=\"Center\"/>\n");
        xml.append("                <text><![CDATA[").append(title).append("]]></text>\n");
        xml.append("            </staticText>\n");
        xml.append("        </band>\n");
        xml.append("    </title>\n");
        
        // Colonnes (en-têtes)
        xml.append("    <columnHeader>\n");
        xml.append("        <band height=\"30\">\n");
        int x = 0;
        int columnWidth = 555 / fieldNames.size();
        for (String fieldName : fieldNames) {
            xml.append("            <staticText>\n");
            xml.append("                <reportElement x=\"").append(x).append("\" y=\"0\" width=\"").append(columnWidth).append("\" height=\"25\"/>\n");
            xml.append("                <box>\n");
            xml.append("                    <pen lineWidth=\"1.0\"/>\n");
            xml.append("                </box>\n");
            xml.append("                <textElement textAlignment=\"Center\" verticalAlignment=\"Middle\"/>\n");
            xml.append("                <text><![CDATA[").append(fieldName).append("]]></text>\n");
            xml.append("            </staticText>\n");
            x += columnWidth;
        }
        xml.append("        </band>\n");
        xml.append("    </columnHeader>\n");
        
        // Détail (données)
        xml.append("    <detail>\n");
        xml.append("        <band height=\"25\">\n");
        x = 0;
        for (String fieldName : fieldNames) {
            xml.append("            <textField isStretchWithOverflow=\"true\">\n");
            xml.append("                <reportElement x=\"").append(x).append("\" y=\"0\" width=\"").append(columnWidth).append("\" height=\"25\" isPrintRepeatedValues=\"false\"/>\n");
            xml.append("                <box>\n");
            xml.append("                    <pen lineWidth=\"0.5\"/>\n");
            xml.append("                </box>\n");
            xml.append("                <textElement textAlignment=\"Left\" verticalAlignment=\"Middle\"/>\n");
            xml.append("                <textFieldExpression><![CDATA[$F{").append(fieldName).append("}]]></textFieldExpression>\n");
            xml.append("            </textField>\n");
            x += columnWidth;
        }
        xml.append("        </band>\n");
        xml.append("    </detail>\n");
        
        xml.append("</jasperReport>");
        
        return new java.io.ByteArrayInputStream(xml.toString().getBytes());
    }
    
    /**
     * Crée un XML de rapport vide
     */
    private InputStream createEmptyReportXml(String title) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\"\n");
        xml.append("             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("             xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\"\n");
        xml.append("             name=\"empty_report\" pageWidth=\"595\" pageHeight=\"842\" columnWidth=\"555\"\n");
        xml.append("             leftMargin=\"20\" rightMargin=\"20\" topMargin=\"20\" bottomMargin=\"20\">\n");
        xml.append("    <title>\n");
        xml.append("        <band height=\"50\">\n");
        xml.append("            <staticText>\n");
        xml.append("                <reportElement x=\"0\" y=\"0\" width=\"555\" height=\"30\"/>\n");
        xml.append("                <textElement textAlignment=\"Center\"/>\n");
        xml.append("                <text><![CDATA[").append(title).append("]]></text>\n");
        xml.append("            </staticText>\n");
        xml.append("            <staticText>\n");
        xml.append("                <reportElement x=\"0\" y=\"30\" width=\"555\" height=\"20\"/>\n");
        xml.append("                <textElement textAlignment=\"Center\"/>\n");
        xml.append("                <text><![CDATA[Aucune donnée disponible]]></text>\n");
        xml.append("            </staticText>\n");
        xml.append("        </band>\n");
        xml.append("    </title>\n");
        xml.append("</jasperReport>");
        
        return new java.io.ByteArrayInputStream(xml.toString().getBytes());
    }
    
    /**
     * Exporte un JasperPrint vers un tableau de bytes PDF
     */
    private byte[] exportToPDF(JasperPrint jasperPrint) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        exporter.exportReport();
        return outputStream.toByteArray();
    }
}

