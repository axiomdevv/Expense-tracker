package com.axiomdevv.expensetracker.data;

import com.axiomdevv.expensetracker.model.Expense;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Loads and saves expenses in a JSON file. */
public class ExpenseRepository {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path dataFile;

    public ExpenseRepository(Path dataFile) {
        this.dataFile = dataFile;
    }

    public List<Expense> load() throws IOException {
        if (Files.notExists(dataFile)) {
            return new ArrayList<>();
        }

        return mapper.readValue(dataFile.toFile(), new TypeReference<>() {
        });
    }

    public void save(List<Expense> expenses) throws IOException {
        Path parent = dataFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path temporaryFile = dataFile.resolveSibling(dataFile.getFileName() + ".tmp");
        mapper.writerWithDefaultPrettyPrinter().writeValue(temporaryFile.toFile(), expenses);

        try {
            Files.move(
                    temporaryFile,
                    dataFile,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, dataFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
