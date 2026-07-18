from langgraph.checkpoint.memory import InMemorySaver


def create_memory_checkpointer() -> InMemorySaver:
    return InMemorySaver()
