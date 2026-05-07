module office.agent.pdf {
    requires java.base;
    requires java.logging;
    requires office.agent.runtime;
    requires office.agent.document;
    requires org.apache.pdfbox;

    exports com.owiseman.pdf;
    exports com.owiseman.pdf.extract;
    exports com.owiseman.pdf.render;
}
