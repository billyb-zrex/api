package com.sharefable.api.transport;

/*
 * TODO This is a temporary placeholder to create an user. User would be created automatically upon signing up for the
 *      first time.
 *      belongsToOrg won't be sent from client side.
 */

public record NewUserReq(String firstName, String lastName, String email, String avatar, Long belongsToOrg) {
}
