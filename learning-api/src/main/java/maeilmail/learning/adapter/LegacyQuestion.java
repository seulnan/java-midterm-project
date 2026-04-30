package maeilmail.learning.adapter;

public record LegacyQuestion(
        Long id,
        String title,
        String content,
        String category
) {
}
