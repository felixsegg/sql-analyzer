# SQL Analyzer

A desktop tool to **generate and evaluate SQL statements with LLMs**. It was built as part of the evaluation for my bachelor’s thesis and is designed to answer three practical questions:

- **Model choice:** Which LLMs produce the most accurate SQL for my use case?
- **Prompt phrasing:** How does the way a user phrases a request affect the generated SQL?
- **Query complexity:** Up to which complexity level do LLMs remain reliable?

---

## What it does

- **Batch generation** of SQL for a matrix of *(LLMs × prompt types × sample queries × repetitions)*.
- **Evaluation** of generated SQL against a reference query using multiple strategies:
    - Structural/syntactic comparison (e.g., section-wise overlap).
    - (Optional) LLM-as-a-judge scoring of semantic similarity.
- **Result management & export**: Persist results and export CSV for analysis.
- **Guided workflow** via JavaFX UI, including in-app help.

---

## Study design (as used in the thesis)

- **LLMs:** ChatGPT (OpenAI), Gemini (Google DeepMind), Claude (Anthropic), DeepSeek (DeepSeek AI).
- **Prompt types (four styles):**
    1. **Descriptive** (natural language explanation)
    2. **Technical** (explicit tables/columns/filters)
    3. **Goal-oriented** (describe the desired outcome only)
    4. **Short/keyword** (very terse keyword lists)
- **Query complexity:** *Simple* / *Medium* / *Complex* (based on joins, subqueries, aggregations).
- **Repetitions:** 5 runs per (LLM, prompt type, sample query) to smooth randomness.
- **Anti-caching temperature sweep:** temperatures distributed uniformly between **0.70 and 0.80**  
  `{0.700, 0.725, 0.750, 0.775, 0.800}`.

> In the thesis, the LLM-as-a-judge evaluation used **Gemini** with sampling disabled (`temperature = 0`) to improve reproducibility.

---

## Architecture

- **Presentation (JavaFX):** Home, Overviews, Details, Workers (generation/evaluation), Settings, Help.
- **Logic/Domain:** Business domain objects (BDOs) and services orchestrating generation & evaluation.
- **Persistence:** DTOs backed by JSON (Gson) for local storage and reproducible exports.

**Key BDOs:**
- `LLM` - configuration of a model/API.
- `SampleQuery` - reference SQL from the production system.
- `PromptType` - one of the four phrasing styles.
- `Prompt` - concrete phrasing per (SampleQuery × PromptType).
- `GeneratedQuery` - LLM-produced SQL and metadata.

---

## Typical workflow

1. Enter **Sample Queries** (reference SQL).
2. Define **Prompt Types**.
3. Add **Prompts** for each sample query × prompt type.
4. Configure **LLMs** (API, model IDs, rate limits, temperature range).
5. Run **Generation** (batch matrix).
6. Run **Evaluation** (choose comparator; LLM-as-judge optional).
7. **Export** results.

---

## Installation & running

- **Requirements:** Java 21+, Maven 3.9+.
- **Run:** from your IDE or via a single command like `mvn javafx:run`.  
  (Details, platform notes, and packaging tips are in the docs.)

> API keys and model IDs are configured in the app’s **Settings**. See the documentation or help windows in the application for the exact fields and storage behavior.

---

## Evaluation approaches

- **Syntactic comparator:** stable and reproducible, but blind to semantically equivalent rewrites.
- **Data-based comparator (Not (yet) implemented):** execute both queries and compare result sets (often impractical due to data needs).
- **LLM-as-a-judge (used in the thesis):** robust to syntax variation, returns a **0-100** semantic similarity score with fine-grained bands.

**Suggested score bands (0-100), as used by the LLM comparator:**
- 0-5: no meaningful relation
- 6-25: extremely different
- 26-45: clearly different, coarse topical overlap
- 46-60: related but not equivalent
- 61-85: close, minor corrections needed
- 86-99: almost equivalent
- 100: semantically equivalent

---

## Reproducibility notes

- **Temperature sweep** reduces vendor-side caching bias across repeated prompts.
- Use **fixed prompts** and **sampling off** for the judge model to improve stability.

---

## Limitations

- LLM-as-a-judge is inherently **model-dependent** and can change with model updates.
- Prompt styles vary across users and contexts; results may **not generalize** to all phrasing.
- The sample set reflects a **specific production domain**; breadth across all SQL tasks is not guaranteed.

---

## Links

- **Developer docs:** https://felixsegg.github.io/sql-analyzer/
- **Thesis (context & methodology):** https://felix.seggebaeing.de/theses/
- **Repository:** https://github.com/felixsegg/sql-analyzer

