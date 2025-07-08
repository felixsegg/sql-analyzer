package logic.util.eval.impl;

import logic.bdo.LLM;
import logic.bdo.SQLQueryWrapper;
import logic.llmapi.LLMException;
import logic.util.eval.StatementComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LLMComparator implements StatementComparator {
    private static final Logger log = LoggerFactory.getLogger(LLMComparator.class);
    
    private final LLM llm;
    private final double temperature;
    
    private final String PROMPT_TEXT = """
            Du bekommst zwei SQL-Select-Statements. Das erste ist eine Musterlösung. Das zweite wurde anhand einer informellen Beschreibung des Ziels des ersten Statements nachempfunden.
            Vergleiche beide Statements hinsichtlich ihrer semantischen Ähnlichkeit. Aliase sind irrelevant. Entscheidend ist ausschließlich, ob ein semantisch äquivalenter Weg gewählt wurde. Die konkrete Syntax spielt nur eine untergeordnete Rolle.
            Wichtig: Gib ausschließlich eine ganze Zahl zwischen 0 und 100 zurück – keinerlei weitere Zeichen als Erklärung, auch ohne zusätzliche Zeichen oder Formatierungen.
            Vermeide Rundungen auf Vielfache von 5, außer sie sind sachlich gerechtfertigt. Nutze feine Abstufungen in Einerschritten. Wähle immer die Zahl, die die tatsächliche semantische Nähe am präzisesten widerspiegelt. Vermeide z.B. 60, 75 oder 85, wenn 62, 76 oder 84 genauer wären.
            Scheue nicht davor zurück, die vollen 100 Punkte zu vergeben, wenn eine semantische Äquivalenz vorliegt.
            
            Bewerte nach folgendem Raster:
            - 0–5: Kein oder kaum erkennbarer Zusammenhang. Eine Umformulierung wäre aufwendiger als ein kompletter Neustart.
            - 6–25: Semantisch extrem unterschiedlich, nur sehr schwache Ähnlichkeit erkennbar.
            - 26–45: Semantisch deutlich verschieden, aber eine grobe thematische Nähe ist vorhanden.
            - 46–60: Semantisch nicht äquivalent, aber mit klar erkennbarer gemeinsamer Grundlage. Überarbeitung wäre mit etwas Erfahrung oder KI-Hilfe gut machbar.
            - 61–85: Semantisch nicht exakt äquivalent, aber der Unterschied ist gering und leicht korrigierbar.
            - 86–99: Semantisch fast äquivalent, Unterschiede nur in minimalen Details.
            - 100: Semantisch vollständig äquivalent; Unterschiede höchstens bei Spaltenauswahl oder -reihenfolge.
            """;
    
    
    public LLMComparator(LLM llm, double temperature) {
        this.llm = llm;
        this.temperature = temperature;
    }
    
    @Override
    public double compare(SQLQueryWrapper query1, SQLQueryWrapper query2) {
        String result = getFromLLM(getFullPrompt(query1.getSql(), query2.getSql()));
        try {
            if (result != null) return Integer.parseInt(result) / 100.0;
        } catch (NumberFormatException e) {
            log.warn("LLM answer did not contain parsable double as requested: {}", result);
        }
        return Double.NaN;
    }
    
    
    private String getFromLLM(String prompt) {
        try {
            return llm.getPromptable().prompt(prompt, llm.getModel(), temperature);
        } catch (LLMException e) {
            log.warn("LLMException occurred while comparing two SQL statements via LLM.", e);
        }
        return null;
    }
    
    private String getFullPrompt(String sampleQuerySQL, String generatedQuerySQL) {
        return PROMPT_TEXT + "\n\nMuster-Statement:\n(\n" + sampleQuerySQL + "\n)\n\nNachempfundenes Statement:\n(\n" + generatedQuerySQL + "\n)";
    }
    
    public LLM getLlm() {
        return llm;
    }
    
    public double getTemperature() {
        return temperature;
    }
}
