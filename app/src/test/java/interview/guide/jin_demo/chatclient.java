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

/**
 * 使用 Spring AI 官方 OpenAI 兼容接口直连阿里云百炼（DashScope）的最小示例。
 * 不依赖项目内任何二次封装。
 */

public class chatclient {

  private static final String API_KEY_ENV = "AI_BAILIAN_API_KEY";
  private static final String MODEL_ENV = "AI_MODEL";
  private static final String DEFAULT_MODEL = "qwen3.5-flash";
  private static final String BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
  @Test
  public void demo1() {
    System.out.println("启动测试");
    String apiKey = requireEnv(API_KEY_ENV);
    String modelName = readEnvOrDefault(MODEL_ENV, DEFAULT_MODEL);

    OpenAiApi openAiApi = buildOpenAiApi(apiKey);
    OpenAiChatModel chatModel = buildChatModel(openAiApi, modelName);
    ChatClient chatClient = ChatClient.builder(chatModel).build();

    String content = chatClient.prompt("你是一个ai助手,可以帮助用户编程").user("请用一句话介绍你自己是什么,功能有哪些")
        .call()
        .content();

    System.out.println("model = " + modelName);
    System.out.println("reply = " + content);
  }

  private static OpenAiApi buildOpenAiApi(String apiKey) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(10000);
    requestFactory.setReadTimeout(120000);

    RestClient.Builder restClientBuilder = RestClient.builder()
        .requestFactory(requestFactory);

    return OpenAiApi.builder()
        .baseUrl(BASE_URL)
        .apiKey(apiKey)
        .completionsPath("/chat/completions")
        .embeddingsPath("/embeddings")
        .restClientBuilder(restClientBuilder)
        .build();
  }

  private static OpenAiChatModel buildChatModel(OpenAiApi openAiApi, String modelName) {
    ToolCallingManager toolCallingManager = ToolCallingManager.builder().build();
    OpenAiChatOptions options = OpenAiChatOptions.builder()
        .model(modelName)
        .temperature(0.2)
        .build();

    return new OpenAiChatModel(
        openAiApi,
        options,
            toolCallingManager,
        RetryUtils.DEFAULT_RETRY_TEMPLATE,
        ObservationRegistry.NOOP
    );
  }

  private static String requireEnv(String key) {
    String value = System.getenv(key);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Missing environment variable: " + key);
    }
    return value;
  }

  private static String readEnvOrDefault(String key, String defaultValue) {
    String value = System.getenv(key);
    return value == null || value.isBlank() ? defaultValue : value;
  }
}
