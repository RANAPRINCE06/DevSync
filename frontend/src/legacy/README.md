# DevSync Legacy Modern UI Archive

This directory contains the previous modern SaaS / Linear / Notion-style UI components and pages.
They are preserved here to guarantee 100% reversible rollback if needed.

## Active UI
The application is currently rendering the **Classic Traditional Developer Portal UI** (top horizontal navigation, boxed panels, data tables, solid borders, compact layout).

## How to Rollback to Legacy UI
1. Replace components in `frontend/src/components/layout/` with the corresponding `.legacy.tsx` files.
2. Replace pages in `frontend/src/pages/` with the corresponding `.legacy.tsx` files.
3. Update `frontend/src/index.css` if necessary.
