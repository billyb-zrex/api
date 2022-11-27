package com.sharefable.api.transport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * TODO This is a temporary placeholder to create an user. User would be created automatically upon signing up for the
 *      first time.
 *      belongsToOrg won't be sent from client side.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewUserReq {
    private String firstName;
    private String lastName;
    private String email;
    private String avatar;
    private Long belongsToOrg;
}
