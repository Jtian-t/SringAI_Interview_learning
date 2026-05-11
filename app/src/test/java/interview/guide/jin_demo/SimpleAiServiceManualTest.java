package interview.guide.jin_demo;

import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 纯手工构建 SimpleAiService 的集成测试，不依赖 Spring Boot 自动配置。
 * 需要设置环境变量 AI_BAILIAN_API_KEY
 */
public class SimpleAiServiceManualTest {



    private static final String API_KEY_ENV = "AI_BAILIAN_API_KEY";
    private static final String BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    private static final String MODEL = "qwen3.5-flash";

    @Test
    void ask_WithRealApi_ShouldReturnNonEmptyAnswer() {
        // 1. 从环境变量读取 API Key
        String apiKey = System.getenv(API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing environment variable: " + API_KEY_ENV);
        }

        // 2. 手动构建 OpenAiApi（配置超时、BaseURL）
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(120000);

        RestClient.Builder restClientBuilder = RestClient.builder()
                .requestFactory(requestFactory);

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(BASE_URL)
                .apiKey(apiKey)
                .completionsPath("/chat/completions")
                .embeddingsPath("/embeddings")
                .restClientBuilder(restClientBuilder)
                .build();

        // 3. 构建 ChatModel（配置模型、温度等）
        ToolCallingManager toolCallingManager = ToolCallingManager.builder().build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(MODEL)
                .temperature(0.2)
                .build();

        OpenAiChatModel chatModel = new OpenAiChatModel(
                openAiApi,
                options,
                toolCallingManager,
                RetryUtils.DEFAULT_RETRY_TEMPLATE,
                ObservationRegistry.NOOP
        );

        // 4. 利用 ChatModel 构建 ChatClient.Builder
        ChatClient.Builder builder = ChatClient.builder(chatModel);

        // 5. 创建 SimpleAiService 实例（就是我们之前定义的那个 Service）
        SimpleAiService simpleAiService = new SimpleAiService(builder);

        // 6. 调用 ask 方法并验证结果
        String answer = simpleAiService.ask("请用一句话介绍你自己，以及你的功能有哪些");
        assertNotNull(answer);
        System.out.println("Model: " + MODEL);
        System.out.println("AI reply: " + answer);
    }
}