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

public class CsvExporter {
    private static final Logger log = LoggerFactory.getLogger(CsvExporter.class);
    
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
