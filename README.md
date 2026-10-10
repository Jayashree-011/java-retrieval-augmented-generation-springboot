# Java RAG API

A beginner-friendly, hands-on project for learning the basics of Retrieval-Augmented Generation (RAG) with Java 21, Spring Boot, and the Gemini API. It demonstrates a backend API workflow: upload a PDF, extract and embed its text, retrieve relevant chunks, and generate an answer grounded in those chunks.

This is an **API development project only**. It does not include a web frontend; use Postman or `curl` to call the endpoints.

## Requirements

- JDK 21
- A Gemini API key from Google AI Studio
- Internet access for Gemini API requests

The Maven wrapper is included, so a separate Maven installation is not required.

## Configure

Set the API key as an environment variable before starting the app. Do not put API keys in source files or commit them.

PowerShell:

```powershell
$env:GEMINI_API_KEY = "your-gemini-api-key"
```

macOS/Linux:

```bash
export GEMINI_API_KEY="your-gemini-api-key"
```

The model names are configured in `src/main/resources/application.yaml`:

- Embeddings: `gemini-embedding-001`
- Chat: `gemini-2.5-flash`

## Run

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
./mvnw spring-boot:run
```

The server starts at `http://localhost:8080`.

## API

### Upload a PDF

Send a `multipart/form-data` request to `/api/documents`. The file field must be named `file`.

```bash
curl -X POST http://localhost:8080/api/documents \
  -F "file=@./document.pdf"
```

In Postman, choose **Body > form-data**, set the key to `file`, set its type to **File**, and select the PDF. Let Postman generate the multipart `Content-Type` header and boundary.

The PDF must contain extractable text; scanned-image PDFs are not OCR-processed.

### Ask a question

Send the question as plain text to `/api/chat`:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: text/plain" \
  --data "What are the main ideas in the document?"
```

The service embeds the question, retrieves up to three relevant chunks, and sends those chunks to Gemini to generate an answer.

## Tests

Windows:

```powershell
.\mvnw.cmd test
```

macOS/Linux:

```bash
./mvnw test
```

## Notes

- The vector store is in memory. Uploaded documents are lost when the application stops.
- Each PDF chunk requires a Gemini embedding request, and each question requires an embedding request plus a generation request. Large PDFs or repeated requests can reach your Gemini API quota.
- `target/`, IDE metadata, environment files, logs, and local upload artifacts are excluded by `.gitignore`.
