package mainpackage;

public record AppData(
        String title,
        String category,    // z.B. "DevTool" oder "Design"
        String description,
        String rating,      // z.B. "★ 4.9"
        String svgPath,     // Der Pfad für das Icon (M12 2L...)
        String badgeText    // Wenn kein Icon, dann Text wie "TS" oder "dB"
) {}