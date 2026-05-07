package com.owiseman.office.ppt;

import com.owiseman.document.model.Block;
import com.owiseman.document.model.Rect;

import java.util.ArrayList;
import java.util.List;

public final class SlideLayoutEngine {

    private static final float DEFAULT_WIDTH = 960f;
    private static final float DEFAULT_HEIGHT = 540f;
    private static final float MARGIN = 40f;
    private static final float LINE_HEIGHT = 30f;

    public List<Rect> layoutBlocks(List<Block> blocks, float slideWidth, float slideHeight) {
        List<Rect> positions = new ArrayList<>();
        float currentY = MARGIN;

        for (Block block : blocks) {
            float blockHeight = estimateBlockHeight(block, slideWidth - 2 * MARGIN);
            float blockWidth = slideWidth - 2 * MARGIN;

            if (currentY + blockHeight > slideHeight - MARGIN) {
                break;
            }

            positions.add(Rect.of(MARGIN, currentY, blockWidth, blockHeight));
            currentY += blockHeight + 10f;
        }

        return positions;
    }

    private float estimateBlockHeight(Block block, float availableWidth) {
        if (block.content() == null || block.content().isBlank()) {
            return LINE_HEIGHT;
        }
        int lineCount = (int) Math.ceil((double) block.content().length() / (availableWidth / 12f));
        return Math.max(lineCount, 1) * LINE_HEIGHT;
    }
}
