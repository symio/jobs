package org.loamok.jobs.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.loamok.jobs.manager.userService;
import org.loamok.jobs.web.request.UserActivateRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
@Tag(name = "Register", description = "Opérations sécurisées sur la création de profils utilisateurs")
public class UserActivateController {

    private final userService userService;

    @Operation(summary = "Active un utilisateur par validation de son adresse e-mail")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Activation effectuée avec succès.",
                content = @Content()
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Token manquant ou invalide",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(
                                type = "object",
                                example = "{ \"error\": \"invalid_request\", \"error_description\": \"Token manquant ou invalide\" }"
                        )
                )
        )
    })
    @PostMapping(value = "/activate",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> activate(
            @Parameter(description = "Objet contenant la clé d'activation", required = true)
            @Valid @RequestBody UserActivateRequest userActivateRequest
    ) {
        try {
            Boolean verified = userService.activateRegisteredUser(userActivateRequest.getKey());
            
            if(verified.equals(Boolean.FALSE))
                return ResponseEntity.badRequest().build();
            
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "Annule l'activation d'un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Activation annulée avec succès.",
                content = @Content()
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Token manquant ou invalide",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(
                                type = "object",
                                example = "{ \"error\": \"invalid_request\", \"error_description\": \"Token manquant ou invalide\" }"
                        )
                )
        )
    })
    @PostMapping(value = "/deactivate",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "Objet contenant la clé d'activation", required = true)
            @Valid @RequestBody UserActivateRequest userActivateRequest
    ) {
        try {
            Boolean verified = userService.deactivateRegisteredUser(userActivateRequest.getKey());
            
            if(verified.equals(Boolean.FALSE))
                return ResponseEntity.badRequest().build();
            
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
