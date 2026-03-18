import { FormEvent, useState } from "react";
import { useEchoMessage } from "@/presentation/hooks/useEchoMessage";

export function EchoMessageForm() {
  const [message, setMessage] = useState("Hello backend");
  const { reply, isLoading, error, send } = useEchoMessage();

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    void send(message);
  }

  return (
    <section className="echo-card">
      <h2>Frontend to backend</h2>
      <p className="echo-copy">
        This message is sent via HTTP to the Java backend and returned as a response.
      </p>

      <form className="echo-form" onSubmit={handleSubmit}>
        <input
          className="echo-input"
          value={message}
          onChange={(event) => setMessage(event.target.value)}
          placeholder="Enter message"
        />
        <button className="echo-button" disabled={isLoading} type="submit">
          {isLoading ? "Sending..." : "Send"}
        </button>
      </form>

      {reply && <p className="echo-reply">{reply}</p>}
      {error && <p className="echo-error">{error}</p>}
    </section>
  );
}
