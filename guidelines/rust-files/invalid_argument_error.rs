use std::fmt;

/// The error of a constructor, setter or function that rejects a value: a value outside of its range, a string with
/// the wrong length or format, a collection with the wrong number of elements (the mapping of the validation
/// annotations and of the errors `invalid-argument-error` and `illegal-format`).
#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct InvalidArgumentError {
    message: String,
}

impl InvalidArgumentError {
    /// Creates a new `InvalidArgumentError`.
    pub fn new(message: impl Into<String>) -> Self {
        Self { message: message.into() }
    }

    /// Returns the description of the problem.
    pub fn message(&self) -> &str {
        &self.message
    }
}

impl fmt::Display for InvalidArgumentError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.write_str(&self.message)
    }
}

impl std::error::Error for InvalidArgumentError {}
