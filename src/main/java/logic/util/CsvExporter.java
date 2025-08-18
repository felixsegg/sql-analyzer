package logic.util;

import logic.bdo.GeneratedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Utility for exporting evaluation scores to a CSV file.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class CsvExporter {
    private static final Logger log = LoggerFactory.getLogger(CsvExporter.class);
    
    /**
     * Exports generated query scores to a CSV file named {@code output.csv} in the given directory.
     * <p>
     * Writes a header and one row per entry with columns:
     * {@code score,llm_name,prompt_type_name,sample_query_name,sample_query_complexity}.
     * Creates parent directories if needed and overwrites any existing file.
     * </p>
     *
     * @param results map of {@link GeneratedQuery} to its score
     * @param path    target directory for the CSV file
     * @throws IOException if creating directories or writing the file fails
     * @implNote Assumes each {@link GeneratedQuery} has non-null generator, prompt, type, and sample query.
     */
    public static void exportScoresCsv(Map<GeneratedQuery, Double> results, String path) throws IOException {
        Path outputFile = Paths.get(path, "output.csv");
        Files.createDirectories(outputFile.getParent());
        
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            writer.write("score,llm_name,prompt_type_name,sample_query_name,sample_query_complexity\n");
            
            for (Map.Entry<GeneratedQuery, Double> entry : results.entrySet()) {
                GeneratedQuery gq = entry.getKey();
                double score = entry.getValue();
                
                String line = String.format("%s,%s,%s,%s,%s",
                        score,
                        gq.getGenerator().getName(),
                        gq.getPrompt().getType().getName(),
                        gq.getPrompt().getSampleQuery().getName(),
                        gq.getPrompt().getSampleQuery().getComplexity().name()
                );
                writer.write(line);
                writer.newLine();
            }
            log.info("Exported output.csv successfully to {}", outputFile.toAbsolutePath());
        }
    }
}
