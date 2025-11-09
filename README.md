# ReadLess

ReadLess is an intelligent web summarizer and text-compression extension powered by Gemini AI.  
It condenses lengthy articles, reports, or research papers into precise, high-density summaries while retaining key insights and contextual integrity.

---

## 🚀 Features

- **Smart Summaries** – Extracts essential ideas in 2–4 compact sentences.
- **Multiple Operations**
    - `summarize` – Dense multi-sentence abstraction.
    - `suggest` – Related topics and deeper readings.
    - `bullets` – Action-oriented key points.
    - `tldr` – One-line essence.
    - `keywords` – Core indexing terms.
    - `qa`, `outline`, `expand`, `shorten`, `translate:<lang>`, etc.
- **Context-Aware Processing** – Preserves factual accuracy and tone.
- **Instant Integration** – Works seamlessly as a browser extension or API endpoint.
- **Lightweight UI** – Minimal distraction; dark and light themes.

---

## 🧠 Tech Stack

| Layer | Technology |
|-------|-------------|
| Backend | Spring Boot 3 / WebFlux |
| AI Engine | Gemini 2.5 Flash API |
| Language Processing | Custom prompt builder with multi-operation routing |
| Frontend / Extension | HTML, JavaScript, TailwindCSS |
| Build & Deploy | Maven, Vercel / Render / Docker |

---

## ⚙️ Configuration

Add your Gemini API key as an environment variable.

```bash
export GEMINI_API_KEY=your_real_api_key