# chatuikit-kotlin Test Structure

Tests organized to mirror the source code structure for easy navigation.

```
kotlin/
├── presentation/                    # UI component tests
│   ├── callbuttons/
│   ├── calllogs/
│   ├── calls/                       # Call flow transition tests
│   ├── conversations/               # Conversations component tests
│   │   ├── style/
│   │   ├── ui/
│   │   └── utils/
│   ├── emojikeyboard/
│   ├── groupmembers/ui/
│   ├── groups/ui/
│   ├── incomingcall/
│   ├── messagecomposer/
│   ├── messageheader/
│   ├── messageinformation/
│   ├── messagelist/
│   │   ├── adapter/
│   │   ├── popupmenu/
│   │   ├── ui/
│   │   └── utils/
│   ├── ongoingcall/
│   ├── outgoingcall/
│   ├── polls/
│   ├── reactionlist/
│   ├── report/
│   ├── search/
│   │   └── adapter/
│   ├── shared/                      # Shared component tests
│   │   ├── baseelements/
│   │   │   ├── avatar/
│   │   │   ├── badgecount/
│   │   │   └── date/
│   │   ├── messagebubble/
│   │   │   ├── actionbubble/
│   │   │   ├── aiassistantbubble/
│   │   │   └── factories/
│   │   ├── receipts/
│   │   ├── statusindicator/
│   │   ├── suggestionlist/
│   │   └── typingindicator/
│   ├── stickerbubble/
│   ├── stickerkeyboard/
│   ├── threadheader/
│   └── users/
└── shared/                          # Shared utility tests
    ├── formatters/
    ├── mentions/
    ├── spans/
    └── views/
```

## Conventions

- Test files use suffixes: `Test`, `PropertyTest`, `IntegrationTest`, `PreservationTest`, `BugExplorationTest`
- Test suites use suffix: `TestSuite`
- Component tests mirror source: `presentation/<component>/`
- Sub-structure mirrors source: `style/`, `ui/`, `adapter/`, `utils/`
- Shared utilities go under `shared/<category>/`
