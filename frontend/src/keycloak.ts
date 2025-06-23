import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
    url: 'http://localhost:8082',
    realm: 'shop-realm',
    clientId: 'frontend-client',
});

export default keycloak;
