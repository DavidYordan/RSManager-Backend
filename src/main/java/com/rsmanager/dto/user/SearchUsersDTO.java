package com.rsmanager.dto.user;

import lombok.*;

import java.util.List;

import com.rsmanager.model.InviterRelationship;
import com.rsmanager.model.PermissionRelationship;
import com.rsmanager.model.RoleRelationship;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchUsersDTO {
    private SearchResponseDTO searchResponseDTO;
    private List<RoleRelationship> roleRelationships;
    private List<PermissionRelationship> permissionRelationships;
    private List<InviterRelationship> inviterRelationships;
}
