package com.owiseman.agent.patch;

import com.owiseman.document.model.Block;
import com.owiseman.document.model.BlockType;
import com.owiseman.document.model.Document;
import com.owiseman.document.model.Page;
import com.owiseman.document.model.SemanticRole;
import com.owiseman.document.model.TextStyle;
import com.owiseman.document.patch.DeleteBlockPatch;
import com.owiseman.document.patch.InsertBlockPatch;
import com.owiseman.document.patch.InsertPagePatch;
import com.owiseman.document.patch.PatchOperation;
import com.owiseman.document.patch.PatchSet;
import com.owiseman.document.patch.ReplaceTextPatch;
import com.owiseman.document.patch.UpdateStylePatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public final class PatchGenerator {

    private static final Logger LOG = Logger.getLogger(PatchGenerator.class.getName());

    public PatchSet generateReplaceText(String documentId, String targetBlockId, String newText) {
        PatchOperation op = new ReplaceTextPatch(targetBlockId, newText);
        return PatchSet.builder()
                .patchSetId("ps-" + UUID.randomUUID().toString().substring(0, 8))
                .documentId(documentId)
                .addOperation(op)
                .build();
    }

    public PatchSet generateInsertBlock(String documentId, int pageIndex, Block block, int position) {
        PatchOperation op = new InsertBlockPatch("insert-" + block.blockId(), pageIndex, block, position);
        return PatchSet.builder()
                .patchSetId("ps-" + UUID.randomUUID().toString().substring(0, 8))
                .documentId(documentId)
                .addOperation(op)
                .build();
    }

    public PatchSet generateDeleteBlock(String documentId, String targetBlockId, int pageIndex) {
        PatchOperation op = new DeleteBlockPatch(targetBlockId, pageIndex);
        return PatchSet.builder()
                .patchSetId("ps-" + UUID.randomUUID().toString().substring(0, 8))
                .documentId(documentId)
                .addOperation(op)
                .build();
    }

    public PatchSet generateUpdateStyle(String documentId, String targetBlockId, TextStyle newStyle) {
        PatchOperation op = new UpdateStylePatch(targetBlockId, newStyle);
        return PatchSet.builder()
                .patchSetId("ps-" + UUID.randomUUID().toString().substring(0, 8))
                .documentId(documentId)
                .addOperation(op)
                .build();
    }

    public PatchSet generateInsertPage(String documentId, int afterPageIndex, Page page) {
        PatchOperation op = new InsertPagePatch("insert-page", afterPageIndex, page);
        return PatchSet.builder()
                .patchSetId("ps-" + UUID.randomUUID().toString().substring(0, 8))
                .documentId(documentId)
                .addOperation(op)
                .build();
    }

    public PatchSet generateFromInstructions(String documentId, List<Map<String, Object>> instructions) {
        List<PatchOperation> operations = new ArrayList<>();

        for (Map<String, Object> inst : instructions) {
            String type = (String) inst.get("type");
            if (type == null) continue;

            switch (type) {
                case "replace_text" -> {
                    String targetId = (String) inst.get("targetId");
                    String newText = (String) inst.get("newText");
                    if (targetId != null && newText != null) {
                        operations.add(new ReplaceTextPatch(targetId, newText));
                    }
                }
                case "insert_block" -> {
                    String targetId = (String) inst.get("targetId");
                    Integer pageIdx = (Integer) inst.get("pageIndex");
                    Integer position = (Integer) inst.get("position");
                    if (targetId != null && pageIdx != null) {
                        Block block = Block.builder()
                                .blockId(UUID.randomUUID().toString().substring(0, 8))
                                .type(BlockType.TEXT)
                                .content((String) inst.getOrDefault("content", ""))
                                .semanticRole(SemanticRole.PARAGRAPH)
                                .build();
                        operations.add(new InsertBlockPatch(targetId, pageIdx, block, position != null ? position : 0));
                    }
                }
                case "delete_block" -> {
                    String targetId = (String) inst.get("targetId");
                    Integer pageIdx = (Integer) inst.get("pageIndex");
                    if (targetId != null && pageIdx != null) {
                        operations.add(new DeleteBlockPatch(targetId, pageIdx));
                    }
                }
                default -> LOG.warning("Unknown instruction type: " + type);
            }
        }

        return PatchSet.builder()
                .patchSetId("ps-" + UUID.randomUUID().toString().substring(0, 8))
                .documentId(documentId)
                .operations(operations)
                .build();
    }
}
