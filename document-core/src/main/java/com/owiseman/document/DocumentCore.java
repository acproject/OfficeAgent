package com.owiseman.document;

public class DocumentCore {

}
public final class Document {
    public List<Page> pages;
}

public final class Page {
    public int pageIndex;
    public List<Block> blocks;
}

public final class Block {
    public int blockId;
    public BlockType type;
    public String content;
    public Rect bbox;
    public SemanticRole semanticRole;
}