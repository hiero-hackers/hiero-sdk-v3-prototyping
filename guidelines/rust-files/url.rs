/// Whether a string is an absolute URL with a host (the check of `@@urlPattern`): a scheme of letters, digits, `+`,
/// `-` and `.` that starts with a letter, followed by `://` and a host without whitespace.
pub fn is_absolute_url(value: &str) -> bool {
    let Some((scheme, rest)) = value.split_once("://") else {
        return false;
    };
    let mut chars = scheme.chars();
    let scheme_valid = chars.next().is_some_and(|c| c.is_ascii_alphabetic())
        && chars.all(|c| c.is_ascii_alphanumeric() || c == '+' || c == '-' || c == '.');
    let host = rest.split(['/', '?', '#']).next().unwrap_or("");
    scheme_valid && !host.is_empty() && !value.chars().any(char::is_whitespace)
}
