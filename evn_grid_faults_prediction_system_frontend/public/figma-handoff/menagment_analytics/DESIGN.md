# Design System Specification: Industrial Authority

## 1. Overview & Creative North Star: "The Digital Superintendent"
This design system moves beyond the "SaaS dashboard" trope to create a high-fidelity, mission-critical environment. Our Creative North Star is **The Digital Superintendent**: an interface that feels like a precision instrument—authoritative, calm under pressure, and immaculately organized.

We break the "template" look by rejecting the standard box-and-border grid. Instead, we use **Intentional Asymmetry** and **Tonal Depth**. Large, high-density data modules are balanced by expansive, airy headers and "glass" overlays. We treat the screen not as a flat webpage, but as a layered control console where information is prioritized through physical stacking and light rather than lines.

---

## 2. Colors: Tonal Architecture
We rely on a "Command & Control" palette. Deep blues provide the foundation of reliability, while high-chroma oranges and reds are reserved strictly for systemic alerts.

### The "No-Line" Rule
**Borders are prohibited for sectioning.** To define a workspace, use background shifts. 
*   **Workspace:** Use `surface` (#f8f9fb).
*   **Sidebar/Navigation:** Use `surface_container_low` (#f2f4f6).
*   **Primary Data Modules:** Use `surface_container_lowest` (#ffffff) to make them "pop" forward.
*   **Nested Elements:** Use `surface_container_high` (#e6e8ea) for secondary information inside a white card.

### Glass & Gradient Implementation
To move beyond a flat "utility" feel:
*   **Map Overlays:** Use `surface_container_lowest` at 80% opacity with a `20px` backdrop-blur. This creates a "frosted glass" command layer over map views.
*   **Primary Actions:** Main CTAs should use a subtle linear gradient from `primary` (#001e42) to `primary_container` (#003368) at a 135-degree angle. This adds "visual soul" and a metallic, industrial sheen.

---

## 3. Typography: Precision Readability
Using **Inter**, we emphasize a "Swiss Editorial" style. Typography is our primary tool for hierarchy.

*   **Display (lg/md):** Reserved for high-level system status or capacity percentages. Use `on_surface` with tight letter-spacing (-0.02em).
*   **Headline (sm/md):** Used for primary section titles. These should feel "heavy" and authoritative.
*   **Body (md/sm):** The workhorse for data tables. Use `on_surface_variant` (#43474f) for secondary metadata to reduce cognitive load in high-density views.
*   **Label (md/sm):** Always uppercase with `0.05em` letter-spacing when used in data headers to evoke a technical, "blueprinted" feel.

---

## 4. Elevation & Depth: Tonal Layering
We do not use shadows to create "pop"; we use light and stacking.

*   **The Layering Principle:** A "floating" utility panel should be `surface_container_lowest` (#ffffff) sitting on a `surface_dim` (#d8dadc) backdrop. The contrast creates the lift.
*   **Ambient Shadows:** If a modal must float (e.g., an emergency shut-off confirmation), use an extra-diffused shadow: `box-shadow: 0 24px 48px -12px rgba(0, 27, 61, 0.08);`. Note the blue tint in the shadow—this maintains the "Deep Blue" brand integrity even in the shadows.
*   **The Ghost Border:** For high-density tables where separation is critical, use `outline_variant` (#c3c6d1) at **15% opacity**. It should be felt, not seen.

---

## 5. Components: The Industrial Toolkit

### High-Density Data Tables
*   **Style:** No vertical or horizontal lines. 
*   **Separation:** Use a `1.5` (0.3rem) spacing gap between rows. Use a subtle `surface_container_low` background on hover.
*   **Typography:** Use `body-sm` for data cells and `label-sm` (all caps) for headers.

### Status Badges (The "Alert" System)
*   **Critical:** `error_container` background with `on_error_container` text.
*   **Warning:** `tertiary_fixed` (#ffdbd1) background with `on_tertiary_fixed_variant` (#872000) text.
*   **Active/Nominal:** `secondary_container` background with `on_secondary_container` text.
*   **Shape:** Use `rounded-sm` (0.125rem) for a more technical, "machined" appearance.

### Buttons & Controls
*   **Primary:** Gradient of `primary` to `primary_container`, `rounded-md`, `label-md` text.
*   **Secondary:** `surface_container_high` background, no border.
*   **Inputs:** Use `surface_container_low` as the field background. On focus, transition to `surface_container_lowest` with a 2px `surface_tint` (#305ea0) bottom-border only.

### Map Overlays & Tooltips
*   **Containers:** Use the "Glassmorphism" rule. Semi-transparent `surface_container_lowest` with `16px` blur.
*   **Padding:** Use `4` (0.9rem) consistently for internal module padding to ensure data "breathes."

---

## 6. Do’s and Don’ts

### Do
*   **Do** use `20` (4.5rem) or `24` (5.5rem) spacing to separate major functional blocks. White space is a sign of system "calm."
*   **Do** stack `surface` colors to create hierarchy. (Lowest = Front, Highest = Back).
*   **Do** use "Safety Orange" (`tertiary` tokens) sparingly. If everything is an alert, nothing is.

### Don't
*   **Don't** use 1px solid black or dark grey borders. They clutter the "mission-critical" clarity.
*   **Don't** use standard "drop shadows." They feel like consumer apps, not industrial tools. Use tonal shifts.
*   **Don't** use "rounded-full" for buttons. Stick to `md` (0.375rem) or `sm` (0.125rem) to maintain a serious, geometric architectural feel.