package interview.guide.jin_demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service  // 如果不用 Spring，这个注解可以去掉
public class SimpleAiService {
    private final ChatClient chatClient;

    public SimpleAiService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String ask(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}