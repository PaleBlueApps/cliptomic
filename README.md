# Cliptomic

Cliptomic is an open-source macOS tool designed to keep you in the flow. It started as a distraction-free AI rewriter triggered by hotkeys, and now it's even more powerful. Use it invisibly for instant edits, or call up the optional UI to switch between multiple LLMs, run advanced queries, and execute quick actions like email replies or translations—all without leaving your current app.

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Shift + Control + Command` | **Instant Rewrite** — Rewrites the text in your clipboard and replaces it with the improved version |
| `Shift + Control + Space` | **Toggle Chatbot** — Opens or closes the chatbot window for interactive queries |

## How It Works

### Invisible Mode (Instant Rewrite)
1. Copy any text to your clipboard
2. Press `Shift + Control + Command`
3. Cliptomic sends your text to the AI, rewrites it, and places the result back in your clipboard
4. Paste the improved text wherever you need it

### Custom Prompts
For one-time custom instructions, use bracket syntax in your clipboard text:
```
[translate to Spanish] Hello, how are you?
```
The text inside brackets becomes your prompt, and everything after gets processed accordingly.

### Chatbot Mode
Press `Shift + Control + Space` to open the chatbot window for:
- Multi-turn conversations
- Complex queries
- Switching between different LLM models

## Use Cases

- **Polish Emails** — Copy your draft, hit the hotkey, paste back a cleaner version
- **Quick Translations** — Use `[translate to French]` or any language
- **Fix Grammar & Style** — Instantly improve any text without context-switching
- **Summarize Content** — Use `[summarize]` to get quick summaries
- **Simplify Text** — Try `[explain like I'm 5]` for simpler explanations
- **Code Help** — Ask the chatbot to explain code snippets or debug errors
- **Reply Drafts** — Use `[write a polite reply to]` for quick email responses
- **Tone Adjustments** — Use `[make this more formal]` or `[make this friendlier]`

## Getting Started

1. Launch Cliptomic — it runs in your menu bar
2. Click the tray icon and open **Settings**
3. Add your [OpenRouter](https://openrouter.ai/) API key
4. Select a model (free and paid options available)
5. Customize the default system prompt and user prompt template (optional)
6. Start using the hotkeys!

## Configuration

In Settings, you can customize:
- **API Key** — Your OpenRouter API key
- **Model** — Choose from preset models or enter a custom model ID
- **System Prompt** — Define the AI's behavior (default: improve clarity, grammar, and style)
- **User Prompt Template** — Template for how your text is sent to the AI (use `{text}` as placeholder)
