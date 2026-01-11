package com.databits.androidscouting.layout;

import com.databits.androidscouting.model.Cell;
import java.util.Collections;
import java.util.List;

/**
 * Result wrapper for layout parsing operations.
 * Provides a type-safe way to return either success with cell data or failure with error message.
 */
public class ParseResult {
    public final List<Cell> cells;
    public final String errorMessage;
    public final boolean isSuccess;

    private ParseResult(List<Cell> cells, String errorMessage, boolean isSuccess) {
        this.cells = cells != null ? cells : Collections.emptyList();
        this.errorMessage = errorMessage;
        this.isSuccess = isSuccess;
    }

    /**
     * Create a successful parse result with cell data
     * @param cells List of cells parsed from JSON
     * @return Success result
     */
    public static ParseResult success(List<Cell> cells) {
        return new ParseResult(cells, null, true);
    }

    /**
     * Create a failed parse result with error message
     * @param message User-friendly error message describing what went wrong
     * @return Error result
     */
    public static ParseResult error(String message) {
        return new ParseResult(null, message, false);
    }
}
