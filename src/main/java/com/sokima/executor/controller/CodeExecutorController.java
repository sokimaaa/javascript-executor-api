package com.sokima.executor.controller;

import com.sokima.executor.model.script.Script;
import com.sokima.executor.model.script.ScriptState;
import com.sokima.executor.script.manager.ScriptStateManager;
import com.sokima.executor.service.ScriptRetryService;
import com.sokima.executor.service.ScriptSearchService;
import com.sokima.executor.service.ScriptStopperService;
import com.sokima.executor.service.ScriptSubmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/code-executor")
public record CodeExecutorController(
        @Qualifier("scriptSubmitterServiceV1") ScriptSubmitterService scriptSubmitterService,
        @Qualifier("scriptRetryServiceV1") ScriptRetryService scriptRetryService,
        @Qualifier("scriptStopperServiceV1") ScriptStopperService scriptStopperService,
        @Qualifier("scriptSearchServiceV1") ScriptSearchService scriptSearchService,
        ScriptStateManager scriptStateManager
) {

    @Operation(
            summary = "Get shorten script state info.",
            description = """
                    Get shorten script state info.
                    Provides only information about execution status,
                    or execution result if executed.
                    """,
            parameters = {
                    @Parameter(
                            name = "scriptId",
                            in = ParameterIn.PATH,
                            description = "The unique identifier of script to get.",
                            required = true,
                            example = "10000"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully got shorten script state result.",
                            content = {
                                    @Content(
                                            schema = @Schema(implementation = ScriptState.class)
                                    )
                            }
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found the script state result with specified identifier."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error."
                    )
            }
    )
    @GetMapping(value = "scripts/{scriptId}/result", produces = MediaType.APPLICATION_JSON_VALUE)
    public ScriptState getExecutionScriptResult(@PathVariable("scriptId") Long scriptId) {
        return scriptSearchService.findScriptExecutionResult(scriptId);
    }

    @Operation(
            summary = "Get detailed script state info.",
            description = """
                    Get detailed script state info.
                    Provides whole information about script state, metrics and so on.
                    """,
            parameters = {
                    @Parameter(
                            name = "scriptId",
                            in = ParameterIn.PATH,
                            description = "The unique identifier of script to get.",
                            required = true,
                            example = "10000"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully got detailed script state result.",
                            content = {
                                    @Content(
                                            schema = @Schema(implementation = ScriptState.class)
                                    )
                            }
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found the script state result with specified identifier."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error."
                    )
            }
    )
    @GetMapping(value = "scripts/{scriptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ScriptState getDetailedScriptState(@PathVariable("scriptId") Long scriptId) {
        return scriptSearchService.findDetailedScriptState(scriptId);
    }

    @Operation(
            summary = "Get a brief info about all user' scripts.",
            description = """
                    Get a brief info about all user' scripts.
                    Provides only needed info for understanding script status and to identify exactly needed.
                    Provides features for sorting, filtering and paging.
                    """,
            parameters = {
                    @Parameter(
                            name = "scriptId",
                            in = ParameterIn.PATH,
                            description = "The unique identifier of script to get.",
                            required = true,
                            example = "10000"
                    ),
                    @Parameter(
                            name = "filterBy",
                            in = ParameterIn.PATH,
                            description = "The string line with filter params to use.",
                            example = "programming_language=eq:JAVASCRIPT;execution_time=gte:P1M"
                    ),
                    @Parameter(
                            name = "sortBy",
                            in = ParameterIn.QUERY,
                            description = "The string list of fields to being sorted.",
                            example = "asc(execution_time),desc(created_at)"
                    ),
                    @Parameter(
                            name = "page",
                            in = ParameterIn.QUERY,
                            description = "The page count to show. Start with 0 by default.",
                            example = "1"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully got detailed script state result.",
                            content = {
                                    @Content(
                                            schema = @Schema(implementation = ScriptState.class)
                                    )
                            }
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error."
                    )
            }
    )
    @GetMapping(value = {"scripts", "scripts/{filterBy}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<ScriptState> getAllBriefScriptsStateOfUser(@RequestParam("userId") String userId,
                                                                 @PathVariable(name = "filterBy", required = false) Optional<String> filterBy,
                                                                 @RequestParam(name = "sortBy", required = false) Optional<String> sortBy,
                                                                 @RequestParam(name = "page", defaultValue = "0") int pageCount) {
        return scriptSearchService.findBriefScriptsState(userId, filterBy, sortBy, pageCount);
    }

    @Operation(
            summary = "Submit script on execution.",
            description = """
                    Submit script on execution. Supported a few way to submit execution:
                    1) Blocking execution - wait till script executes and get response instantly.
                    2) Concurrent execution - submit execution in thread pool and monitor status by your own.
                    Supported scheduling feature, in case when you want to delay execution of script.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Instant content to submit on execution.",
                    content = @Content(
                            schema = @Schema(implementation = Script.class)
                    ),
                    required = true
            ),
            parameters = {
                    @Parameter(
                            name = "blocking",
                            in = ParameterIn.QUERY,
                            description = "The boolean that specify the chosen way of execution.",
                            example = "true"
                    ),
                    @Parameter(
                            name = "schedule",
                            in = ParameterIn.QUERY,
                            description = "The duration that indicates after which time period should be submitted execution.",
                            example = "P2M"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Script is accepted on execution.",
                            content = {
                                    @Content(
                                            schema = @Schema(implementation = ScriptState.class)
                                    )
                            }
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Script wasn't accepted or executed successfully due invalid data."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error."
                    )
            }
    )
    @PostMapping(value = "scripts", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScriptState> submitExecutionScript(@RequestBody @Valid Script script,
                                                             @RequestParam(name = "blocking", defaultValue = "false") boolean isBlockingExecution,
                                                             @RequestParam(name = "schedule", required = false) Optional<String> scheduledTime) {
        Optional<ScriptState> scriptState = scriptSubmitterService.submit(script, isBlockingExecution, scheduledTime);
        return scriptState
                .map(state -> ResponseEntity.status(HttpStatus.CREATED).body(state))
                .orElseGet(() -> ResponseEntity.accepted().build());
    }

    @Operation(
            summary = "Retry script execution.",
            description = """
                    Retrying script execution if script wasn't removed or marked as malicious.
                    Script state will be rewritten after retrying.
                    """,
            parameters = {
                    @Parameter(
                            name = "scriptId",
                            in = ParameterIn.PATH,
                            description = "The unique identifier of script to retry.",
                            example = "10000"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Script is accepted on retrying.",
                            content = {
                                    @Content(
                                            schema = @Schema(implementation = ScriptState.class)
                                    )
                            }
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Script wasn't retried due invalid state."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error."
                    )
            }
    )
    @PutMapping(value = "scripts/{scriptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScriptState> retryExecutionScript(@PathVariable("scriptId") Long scriptId) {
        ScriptState scriptState = scriptRetryService.retryScript(scriptId);
        return ResponseEntity.accepted().body(scriptState);
    }

    @Operation(
            summary = "Stop script execution.",
            description = """
                    Stop script execution.
                    Only applicable for running or queuing scripts.
                    There two ways of stopping:
                    1) Safe-stop: wait till execution ended.
                    2) Force-stop: immediately stop execution.
                    """,
            parameters = {
                    @Parameter(
                            name = "scriptId",
                            in = ParameterIn.PATH,
                            description = "The unique identifier of script to retry.",
                            example = "10000"
                    ),
                    @Parameter(
                            name = "forced",
                            in = ParameterIn.QUERY,
                            description = "The way to stop execution.",
                            example = "true"

                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Script is accepted on stopping.",
                            content = {
                                    @Content(
                                            schema = @Schema(implementation = ScriptState.class)
                                    )
                            }
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Script wasn't stopped due invalid data."
                    ),
                    @ApiResponse(
                            responseCode = "405",
                            description = "Script wasn't stopped due invalid state."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error."
                    )
            }
    )
    @PatchMapping(value = "scripts/{scriptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScriptState> stopExecutionScript(@PathVariable("scriptId") Long scriptId,
                                                           @RequestParam(name = "forced", defaultValue = "false") boolean isForced) {
        ScriptState scriptState = scriptStopperService.stopExecution(scriptId, isForced);
        return ResponseEntity.accepted().body(scriptState);
    }

    @Operation(
            summary = "Remove script and script state from saved.",
            description = """
                    Remove script and script state.
                    Only applicable for ended scripts (COMPLETED, CANCELLED, or STOPPED).
                    Otherwise, will be raised exception and removing will be unsuccessful.
                    """,
            parameters = {
                    @Parameter(
                            name = "scriptId",
                            in = ParameterIn.PATH,
                            description = "The unique identifier of script to remove.",
                            example = "10000"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Script is accepted on removing."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Script wasn't found to remove."
                    ),
                    @ApiResponse(
                            responseCode = "405",
                            description = "Script wasn't removed due illegal execution state."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error."
                    )
            }
    )
    @DeleteMapping(value = "scripts/{scriptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> removeInactiveJavaScripts(@PathVariable("scriptId") Long scriptId) {
        scriptStateManager.removeScript(scriptId);
        return ResponseEntity.noContent().build();
    }
}
