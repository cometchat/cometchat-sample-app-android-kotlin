# chatuikit-compose Test Structure

Instrumentation tests organized under `presentation/` to mirror the source code component structure.

```
compose/
└── presentation/                    # All UI component tests
    ├── callbuttons/ui/
    ├── calllogs/ui/
    ├── conversations/ui/
    ├── createpoll/ui/
    ├── emojikeyboard/ui/
    ├── groupmembers/ui/
    ├── groups/ui/
    ├── imageviewer/ui/
    ├── incomingcall/ui/
    ├── messagecomposer/
    │   └── formatter/               # Rich text formatting tests
    ├── messageheader/ui/
    ├── messageinformation/ui/
    ├── messagelist/ui/
    ├── ongoingcall/ui/
    ├── outgoingcall/ui/
    ├── reactionlist/ui/
    ├── search/ui/
    ├── shared/                      # Shared component tests
    │   ├── baseelements/            # Avatar, BadgeCount, Date
    │   ├── defaultstates/           # Error, Loading states
    │   ├── dialog/
    │   ├── events/
    │   ├── mentions/
    │   └── permission/
    ├── stickerkeyboard/ui/
    ├── threadheader/ui/
    └── users/ui/
```

## Conventions

- Test files use suffixes: `Test`, `InstrumentationTest`, `PropertyTest`, `IntegrationTest`
- Test suites use suffix: `TestSuite`
- Component tests go under `presentation/<component>/ui/`
- Shared/cross-cutting tests go under `presentation/shared/<category>/`
- Formatter/rich-text tests go under `presentation/messagecomposer/formatter/`
