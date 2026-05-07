module office.agent.office {
    requires java.base;
    requires java.desktop;
    requires java.logging;
    requires office.agent.runtime;
    requires office.agent.document;
    requires org.apache.poi.ooxml;

    exports com.owiseman.office;
    exports com.owiseman.office.ppt;
    exports com.owiseman.office.word;
    exports com.owiseman.office.excel;
}
