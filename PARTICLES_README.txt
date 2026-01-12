Vykna Client - Particle System (client-only)
==================================================

What you got:
- Allocation-free particle system (fixed pool, no per-frame allocations)
- Procedural glow sprites (no cache edits needed)
- Data-driven presets + attachments (data/particles/*.json)

Demo effect:
- Santa hat (item id 1050) emits subtle sparkles above the player's head.

Files added:
- src/main/java/com/client/particles/* (particle engine)
- data/particles/presets.json
- data/particles/attachments.json

How to test:
1) Build/run the client as normal.
2) Spawn and equip a Santa hat (item 1050).
3) You should see sparkles above the head while it's equipped.

Tuning:
- Edit data/particles/presets.json (rate, lifetime, size, gravity, etc).
- Add more item attachments in data/particles/attachments.json

Safety:
- Global cap: 3500 particles max
- Emission hard-cap per player per frame (max 8)
- Delta-time clamped to avoid bursts after alt-tab


NOTE (v2): Particle JSON now loads from the client cache folder first (Signlink.getCacheDirectory()/particles/),
so it works consistently from IDE or a packaged jar. If the files don't exist, they are created with defaults.
