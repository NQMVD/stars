# Remaining Work: AppVault High-Fidelity Migration

This document outlines the remaining tasks to achieve 100% feature and visual parity with the AppVault website.

## 1. Visual Polish & Color Accuracy
- [ ] **Global Color Audit**: Perform a final pass on all views to ensure every hex code matches the extracted website colors.
    - *Focus*: Borders, hover states, scrollbars, and text secondary colors.
- [ ] **Font Weights**: Verify that "Geist" font weights (Regular vs Medium vs Bold) are applied correctly across all headers and labels.
- [ ] **Icon Colors**: Ensure all icons (sidebar, top bar, cards) use the exact extracted gray/white values.

## 2. Functional Components
- [x] **Tab Selectors**: Implement interactive tab switching logic.
    - *Locations*: `AppDetailView` (Overview/Reviews/etc), `LibraryView` (Installed/Updates), `TopBar` (Platform filters).
    - *Behavior*: Visual state change (active/inactive) and content swapping.
- [x] **Search Functionality**: Implement real-time filtering in `TopBar`.
    - *Behavior*: Typing in the search bar should filter the visible cards in `HomeView` or `CategoryView`.
- [x] **Carousel/Gallery**: Make the screenshot gallery in `AppDetailView` interactive.
    - *Behavior*: Clicking arrows cycles through images (placeholders).

## 3. Missing Views
- [x] **Settings Page**: Implement the full Settings view.
    - *Content*: General, Appearance (Theme toggle), Account, About.
    - *Style*: Consistent with the rest of the app (dark theme, standard form controls).
- [x] **Category Views**: Differentiate category pages.
    - *Current*: Specific content for "Developer Tools", "Productivity", "Graphics & Design", "Games".

## 4. Interactivity & UX
- [x] **Install/Open Logic**: Implement mock state changes.
    - *Behavior*: Clicking "Install" shows "Installing..." (mock delay) then changes to "Open".
- [x] **Back Navigation**: Ensure "Back" buttons work consistently across all deep views.
- [ ] **Hover Effects**: Refine hover states for cards and buttons to match the website's subtle transitions.

## 5. Final Polish
- [ ] **Responsive Layout**: Ensure the grid adapts gracefully to window resizing.
- [ ] **Scrollbars**: Style JavaFX scrollbars to look like the custom web scrollbars (thin, dark).
