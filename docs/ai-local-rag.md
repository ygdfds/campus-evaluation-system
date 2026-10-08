# Local AI Assistant

The assistant uses Ollama locally. It does not call a paid cloud model.

## Models

The default models are:

- `qwen2.5:3b` for answer generation
- `nomic-embed-text` for document embeddings

The backend reads the current user's tenant, school, role-visible help FAQs, operation guides, published announcements, and school profile as the RAG knowledge base.

## Configuration

Set these variables in the backend `.env` file when a different Ollama installation or model is needed:

```properties
OLLAMA_BASE_URL=http://127.0.0.1:11434
OLLAMA_CHAT_MODEL=qwen2.5:3b
OLLAMA_EMBEDDING_MODEL=nomic-embed-text
OLLAMA_TIMEOUT_SECONDS=120
```

Start Ollama, download the models, then start the backend:

```powershell
ollama serve
ollama pull qwen2.5:3b
ollama pull nomic-embed-text
mvn spring-boot:run -pl campus-admin -am
```

The authenticated frontend exposes the `AI 问答` button in the student, staff, school administrator, and system administrator workspaces.
