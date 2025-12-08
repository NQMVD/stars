package mainpackage;

public record AppData(
        String title,
        String author, // z.B. "Microsoft"
        String category, // z.B. "DevTool" oder "Design"
        String description,
        String rating, // z.B. "★ 4.9"
        String price, // z.B. "Free"
        boolean isInstalled,
        String color, // CSS Color/Gradient, e.g. "#3b82f6" or "linear-gradient(...)"
        String svgPath, // Der Pfad für das Icon (M12 2L...)
        String badgeText // Wenn kein Icon, dann Text wie "TS" oder "dB"
) {
}