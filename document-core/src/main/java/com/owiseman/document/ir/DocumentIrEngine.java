package com.owiseman.document.ir;

import com.owiseman.document.model.Block;
import com.owiseman.document.model.Document;
import com.owiseman.document.model.Page;
import com.owiseman.document.patch.DeleteBlockPatch;
import com.owiseman.document.patch.DeletePagePatch;
import com.owiseman.document.patch.InsertBlockPatch;
import com.owiseman.document.patch.InsertPagePatch;
import com.owiseman.document.patch.MergeBlocksPatch;
import com.owiseman.document.patch.MoveBlockPatch;
import com.owiseman.document.patch.PatchOperation;
import com.owiseman.document.patch.PatchSet;
import com.owiseman.document.patch.ReplaceImagePatch;
import com.owiseman.document.patch.ReplaceTextPatch;
import com.owiseman.document.patch.UpdateMetadataPatch;
import com.owiseman.document.patch.UpdateStylePatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class DocumentIrEngine {

    private static final Logger LOG = Logger.getLogger(DocumentIrEngine.class.getName());

    public Document apply(Document document, PatchSet patchSet) {
        if (!document.documentId().equals(patchSet.documentId())) {
            throw new IllegalArgumentException("PatchSet document ID does not match Document ID");
        }

        Document.Builder docBuilder = document.toBuilder();
        List<Page> pages = new ArrayList<>(document.pages());

        for (PatchOperation op : patchSet.operations()) {
            pages = applyOperation(pages, op);
        }

        docBuilder.pages(pages);
        LOG.info("Applied " + patchSet.operations().size() + " patches to document " + document.documentId());
        return docBuilder.build();
    }

    private List<Page> applyOperation(List<Page> pages, PatchOperation op) {
        return switch (op) {
            case ReplaceTextPatch p -> applyReplaceText(pages, p);
            case InsertBlockPatch p -> applyInsertBlock(pages, p);
            case DeleteBlockPatch p -> applyDeleteBlock(pages, p);
            case MoveBlockPatch p -> applyMoveBlock(pages, p);
            case UpdateStylePatch p -> applyUpdateStyle(pages, p);
            case InsertPagePatch p -> applyInsertPage(pages, p);
            case DeletePagePatch p -> applyDeletePage(pages, p);
            case UpdateMetadataPatch p -> pages;
            case ReplaceImagePatch p -> applyReplaceImage(pages, p);
            case MergeBlocksPatch p -> applyMergeBlocks(pages, p);
        };
    }

    private List<Page> applyReplaceText(List<Page> pages, ReplaceTextPatch patch) {
        List<Page> result = new ArrayList<>();
        for (Page page : pages) {
            Page.Builder pb = page.toBuilder();
            List<Block> newBlocks = new ArrayList<>();
            for (Block block : page.blocks()) {
                if (block.blockId().equals(patch.targetId())) {
                    newBlocks.add(block.toBuilder().content(patch.newText()).build());
                } else {
                    newBlocks.add(block);
                }
            }
            pb.blocks(newBlocks);
            result.add(pb.build());
        }
        return result;
    }

    private List<Page> applyInsertBlock(List<Page> pages, InsertBlockPatch patch) {
        List<Page> result = new ArrayList<>(pages);
        if (patch.pageIndex() < 0 || patch.pageIndex() >= result.size()) {
            LOG.warning("InsertBlock: invalid page index " + patch.pageIndex());
            return result;
        }
        Page page = result.get(patch.pageIndex());
        Page.Builder pb = page.toBuilder();
        List<Block> blocks = new ArrayList<>(page.blocks());
        int pos = Math.min(patch.position(), blocks.size());
        blocks.add(pos, patch.block());
        pb.blocks(blocks);
        result.set(patch.pageIndex(), pb.build());
        return result;
    }

    private List<Page> applyDeleteBlock(List<Page> pages, DeleteBlockPatch patch) {
        List<Page> result = new ArrayList<>();
        for (Page page : pages) {
            Page.Builder pb = page.toBuilder();
            List<Block> newBlocks = new ArrayList<>();
            for (Block block : page.blocks()) {
                if (!block.blockId().equals(patch.targetId())) {
                    newBlocks.add(block);
                }
            }
            pb.blocks(newBlocks);
            result.add(pb.build());
        }
        return result;
    }

    private List<Page> applyMoveBlock(List<Page> pages, MoveBlockPatch patch) {
        Block toMove = null;
        List<Page> result = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            Page page = pages.get(i);
            if (i == patch.fromPageIndex()) {
                Page.Builder pb = page.toBuilder();
                List<Block> newBlocks = new ArrayList<>();
                for (Block block : page.blocks()) {
                    if (block.blockId().equals(patch.targetId())) {
                        toMove = block;
                    } else {
                        newBlocks.add(block);
                    }
                }
                pb.blocks(newBlocks);
                result.add(pb.build());
            } else {
                result.add(page);
            }
        }
        if (toMove != null && patch.toPageIndex() < result.size()) {
            Page targetPage = result.get(patch.toPageIndex());
            Page.Builder pb = targetPage.toBuilder();
            List<Block> blocks = new ArrayList<>(targetPage.blocks());
            int pos = Math.min(patch.position(), blocks.size());
            blocks.add(pos, toMove);
            pb.blocks(blocks);
            result.set(patch.toPageIndex(), pb.build());
        }
        return result;
    }

    private List<Page> applyUpdateStyle(List<Page> pages, UpdateStylePatch patch) {
        List<Page> result = new ArrayList<>();
        for (Page page : pages) {
            Page.Builder pb = page.toBuilder();
            List<Block> newBlocks = new ArrayList<>();
            for (Block block : page.blocks()) {
                if (block.blockId().equals(patch.targetId())) {
                    newBlocks.add(block.toBuilder().style(patch.newStyle()).build());
                } else {
                    newBlocks.add(block);
                }
            }
            pb.blocks(newBlocks);
            result.add(pb.build());
        }
        return result;
    }

    private List<Page> applyInsertPage(List<Page> pages, InsertPagePatch patch) {
        List<Page> result = new ArrayList<>(pages);
        int insertAt = Math.min(patch.afterPageIndex() + 1, result.size());
        result.add(insertAt, patch.page());
        for (int i = 0; i < result.size(); i++) {
            Page p = result.get(i);
            if (p.pageIndex() != i) {
                result.set(i, p.toBuilder().pageIndex(i).build());
            }
        }
        return result;
    }

    private List<Page> applyDeletePage(List<Page> pages, DeletePagePatch patch) {
        List<Page> result = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            if (i != patch.pageIndex()) {
                result.add(pages.get(i));
            }
        }
        for (int i = 0; i < result.size(); i++) {
            Page p = result.get(i);
            if (p.pageIndex() != i) {
                result.set(i, p.toBuilder().pageIndex(i).build());
            }
        }
        return result;
    }

    private List<Page> applyReplaceImage(List<Page> pages, ReplaceImagePatch patch) {
        List<Page> result = new ArrayList<>();
        for (Page page : pages) {
            Page.Builder pb = page.toBuilder();
            List<Block> newBlocks = new ArrayList<>();
            for (Block block : page.blocks()) {
                if (block.blockId().equals(patch.targetId())) {
                    newBlocks.add(block.toBuilder()
                            .metadata("imageData", patch.imageData())
                            .metadata("imageFormat", patch.format())
                            .build());
                } else {
                    newBlocks.add(block);
                }
            }
            pb.blocks(newBlocks);
            result.add(pb.build());
        }
        return result;
    }

    private List<Page> applyMergeBlocks(List<Page> pages, MergeBlocksPatch patch) {
        List<Page> result = new ArrayList<>();
        for (Page page : pages) {
            Page.Builder pb = page.toBuilder();
            List<Block> newBlocks = new ArrayList<>();
            boolean merged = false;
            for (Block block : page.blocks()) {
                if (patch.blockIds().contains(block.blockId())) {
                    if (!merged) {
                        newBlocks.add(block.toBuilder()
                                .blockId(patch.targetId())
                                .content(patch.mergedContent())
                                .build());
                        merged = true;
                    }
                } else {
                    newBlocks.add(block);
                }
            }
            pb.blocks(newBlocks);
            result.add(pb.build());
        }
        return result;
    }
}
