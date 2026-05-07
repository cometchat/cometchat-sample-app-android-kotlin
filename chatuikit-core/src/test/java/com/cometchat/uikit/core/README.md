# chatuikit-core Test Structure

Tests are organized to mirror the source code structure for easy navigation.

```
core/
├── constants/          # SearchFilter, SearchMode tests
├── data/
│   ├── datasource/     # DataSource implementation tests
│   └── repository/     # Repository implementation tests
├── domain/
│   ├── model/          # Domain model tests
│   └── usecase/        # UseCase tests
├── events/             # Event class tests
├── factory/            # ViewModel factory tests
├── formatter/          # Rich text formatting tests
├── mentions/           # Mention detection/insertion tests
├── models/             # Model class tests
├── resources/          # Localization, sound manager tests
├── state/              # UI state tests
├── testutils/          # Shared test utilities (MockFactory)
├── utils/              # Utility class tests
└── viewmodel/          # ViewModel tests (grouped by component)
    ├── callbuttons/
    ├── calllogs/
    ├── conversations/
    ├── events/
    ├── groupmembers/
    ├── groups/
    ├── incomingcall/
    ├── listoperations/
    ├── mediarecorder/
    ├── messagecomposer/
    ├── messageheader/
    ├── messageinformation/
    ├── messagelist/
    ├── ongoingcall/
    ├── outgoingcall/
    ├── reactionlist/
    ├── search/
    └── users/
```

## Conventions

- Test files use suffixes: `Test`, `PropertyTest`, `IntegrationTest`, `PreservationTest`, `BugExplorationTest`
- Test suites use suffix: `TestSuite`
- Disabled tests use `.bak` or `.bak2` extension
- Shared test utilities go in `testutils/`
