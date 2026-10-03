# Service API

## Description

Types shared by the enterprise services. A `Subscription` represents an active subscription; call `unsubscribe()` to
stop receiving updates.

## API Schema

```
namespace enterprise.service.common

Subscription {
    unsubscribe();
}

```