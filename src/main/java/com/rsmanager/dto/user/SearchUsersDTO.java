package com.rsmanager.dto.user;

import lombok.*;

import java.util.List;

import com.rsmanager.model.InviterRelationship;
import com.rsmanager.model.RolePermissionRelationship;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchUsersDTO {
    private SearchResponseDTO searchResponseDTO;
    private List<RolePermissionRelationship> rolePermissionRelationships;
    private List<InviterRelationship> inviterRelationships;
}
