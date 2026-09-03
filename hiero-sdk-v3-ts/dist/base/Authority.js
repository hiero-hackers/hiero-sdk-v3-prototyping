export function evaluateAuthority(auth) {
    switch (auth.kind) {
        case 'PublicKeyAuthority':
            return 1;
        case 'ContractAuthority':
            return 2;
        case 'AuthorityList':
            return auth.children.reduce((acc, child) => acc + evaluateAuthority(child), 0);
    }
}
