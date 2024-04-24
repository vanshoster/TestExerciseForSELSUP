package org.example;

import java.util.concurrent.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

public class CrptApi {
    // Семафор дял контроля максимально количество параллельных потоков
    private final Semaphore semaphore;
    // HTTP клиент
    private final HttpClient httpClient;
    // Время ожидания отправки запроса
    private final Duration timeout;

    // Конструктор класса
    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.semaphore = new Semaphore(requestLimit);
        this.timeout = Duration.ofMillis(timeUnit.toMillis(1)); // Преобразуем единицу времени в миллисекунды.
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    // Метод для создания документа через API
    public boolean createDocument(String jsonDocument, String signature) {
        try {
            semaphore.acquire(); // Получаем разрешение от семафора
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                    .header("Content-Type", "application/json")
                    .header("Signature", signature)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonDocument))
                    .timeout(timeout)
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            semaphore.release(); // Освобождение семафора после выполнения запроса
        }
        return false;
    }
}

