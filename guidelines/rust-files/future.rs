use std::future::Future;
use std::pin::Pin;

/// The result of an asynchronous trait method (the mapping of `@@async` in a trait): a boxed future that can be sent
/// to other threads. Await it like any other future: `let receipt = response.query_receipt().await;`.
pub type BoxFuture<'a, T> = Pin<Box<dyn Future<Output = T> + Send + 'a>>;

/// The result of a streaming method (the mapping of `@@streaming`): a boxed, pull-based asynchronous stream of items
/// that can be sent to other threads. Dropping the stream cancels it.
pub type BoxStream<'a, T> = Pin<Box<dyn futures_core::Stream<Item = T> + Send + 'a>>;
