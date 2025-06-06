/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.modelcontextprotocol.spec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.util.Assert;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Based on the <a href="http://www.jsonrpc.org/specification">JSON-RPC 2.0
 * specification</a> and the <a href=
 * "https://github.com/modelcontextprotocol/specification/blob/main/schema/2024-11-05/schema.ts">Model
 * Context Protocol Schema</a>.
 *
 * @author Christian Tzolov
 */
public final class McpSchema {

	private static final Logger logger = LoggerFactory.getLogger(McpSchema.class);

	private McpSchema() {
	}

	public static final String LATEST_PROTOCOL_VERSION = "2024-11-05";

	public static final String JSONRPC_VERSION = "2.0";

	// ---------------------------
	// Method Names
	// ---------------------------

	// Lifecycle Methods
	public static final String METHOD_INITIALIZE = "initialize";

	public static final String METHOD_NOTIFICATION_INITIALIZED = "notifications/initialized";

	public static final String METHOD_PING = "ping";

	// Tool Methods
	public static final String METHOD_TOOLS_LIST = "tools/list";

	public static final String METHOD_TOOLS_CALL = "tools/call";

	public static final String METHOD_NOTIFICATION_TOOLS_LIST_CHANGED = "notifications/tools/list_changed";

	// Resources Methods
	public static final String METHOD_RESOURCES_LIST = "resources/list";

	public static final String METHOD_RESOURCES_READ = "resources/read";

	public static final String METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED = "notifications/resources/list_changed";

	public static final String METHOD_RESOURCES_TEMPLATES_LIST = "resources/templates/list";

	public static final String METHOD_RESOURCES_SUBSCRIBE = "resources/subscribe";

	public static final String METHOD_RESOURCES_UNSUBSCRIBE = "resources/unsubscribe";

	// Prompt Methods
	public static final String METHOD_PROMPT_LIST = "prompts/list";

	public static final String METHOD_PROMPT_GET = "prompts/get";

	public static final String METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED = "notifications/prompts/list_changed";

	public static final String METHOD_COMPLETION_COMPLETE = "completion/complete";

	// Logging Methods
	public static final String METHOD_LOGGING_SET_LEVEL = "logging/setLevel";

	public static final String METHOD_NOTIFICATION_MESSAGE = "notifications/message";

	// Roots Methods
	public static final String METHOD_ROOTS_LIST = "roots/list";

	public static final String METHOD_NOTIFICATION_ROOTS_LIST_CHANGED = "notifications/roots/list_changed";

	// Sampling Methods
	public static final String METHOD_SAMPLING_CREATE_MESSAGE = "sampling/createMessage";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	// ---------------------------
	// JSON-RPC Error Codes
	// ---------------------------

	/**
	 * Standard error codes used in MCP JSON-RPC responses.
	 */
	public static final class ErrorCodes {

		/**
		 * Invalid JSON was received by the server.
		 */
		public static final int PARSE_ERROR = -32700;

		/**
		 * The JSON sent is not a valid Request object.
		 */
		public static final int INVALID_REQUEST = -32600;

		/**
		 * The method does not exist / is not available.
		 */
		public static final int METHOD_NOT_FOUND = -32601;

		/**
		 * Invalid method parameter(s).
		 */
		public static final int INVALID_PARAMS = -32602;

		/**
		 * Internal JSON-RPC error.
		 */
		public static final int INTERNAL_ERROR = -32603;

	}

	public interface Request {

	}

	private static final TypeReference<HashMap<String, Object>> MAP_TYPE_REF = new TypeReference<HashMap<String,Object>>() {
	};

	/**
	 * Deserializes a JSON string into a JSONRPCMessage object.
	 * @param objectMapper The ObjectMapper instance to use for deserialization
	 * @param jsonText The JSON string to deserialize
	 * @return A JSONRPCMessage instance using either the {@link JSONRPCRequest},
	 * {@link JSONRPCNotification}, or {@link JSONRPCResponse} classes.
	 * @throws IOException If there's an error during deserialization
	 * @throws IllegalArgumentException If the JSON structure doesn't match any known
	 * message type
	 */
	public static JSONRPCMessage deserializeJsonRpcMessage(ObjectMapper objectMapper, String jsonText)
			throws IOException {

		logger.debug("Received JSON message: {}", jsonText);

		Map<String, Object> map = objectMapper.readValue(jsonText, MAP_TYPE_REF);

		// Determine message type based on specific JSON structure
		if (map.containsKey("method") && map.containsKey("id")) {
			return objectMapper.convertValue(map, JSONRPCRequest.class);
		}
		else if (map.containsKey("method") && !map.containsKey("id")) {
			return objectMapper.convertValue(map, JSONRPCNotification.class);
		}
		else if (map.containsKey("result") || map.containsKey("error")) {
			return objectMapper.convertValue(map, JSONRPCResponse.class);
		}

		throw new IllegalArgumentException("Cannot deserialize JSONRPCMessage: " + jsonText);
	}

	// ---------------------------
	// JSON-RPC Message Types
	// ---------------------------
	public interface JSONRPCMessage {

		String jsonrpc();

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class JSONRPCRequest implements JSONRPCMessage {

		@JsonProperty("jsonrpc")
		String jsonrpc;

		@JsonProperty("method")
		String method;

		@JsonProperty("id")
		Object id;

		@JsonProperty("params")
		Object params;

		public JSONRPCRequest() {

		}
		public JSONRPCRequest(String jsonrpc, String method, Object id, Object params) {
			this.jsonrpc = jsonrpc;
			this.method = method;
			this.id = id;
			this.params = params;
		}

		@JsonProperty("jsonrpc")
		public String jsonrpc() {
			return jsonrpc;
		}

		@JsonProperty("method")
		public String method() {
			return method;
		}

		@JsonProperty("id")
		public Object id() {
			return id;
		}

		@JsonProperty("params")
		public Object params() {
			return params;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			JSONRPCRequest that = (JSONRPCRequest) o;
			return Objects.equals(jsonrpc, that.jsonrpc) && Objects.equals(method, that.method)
					&& Objects.equals(id, that.id) && Objects.equals(params, that.params);
		}

		@Override
		public int hashCode() {
			return Objects.hash(jsonrpc, method, id, params);
		}

		@Override
		public String toString() {
			return "JSONRPCRequest{" + "jsonrpc='" + jsonrpc + '\'' + ", method='" + method + '\'' + ", id=" + id
					+ ", params=" + params + '}';
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class JSONRPCNotification implements JSONRPCMessage {

		@JsonProperty("jsonrpc")
		String jsonrpc;

		@JsonProperty("method")
		String method;

		@JsonProperty("params")
		Object params;

		public JSONRPCNotification() {

		}
		public JSONRPCNotification(String jsonrpc, String method, Object params) {
			this.jsonrpc = jsonrpc;
			this.method = method;
			this.params = params;
		}

		@Override
		public String jsonrpc() {
			return jsonrpc;
		}

		public String method() {
			return method;
		}

		public Object params() {
			return params;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class JSONRPCResponse implements JSONRPCMessage {

		@JsonProperty("jsonrpc")
		String jsonrpc;

		@JsonProperty("id")
		Object id;

		@JsonProperty("result")
		Object result;

		@JsonProperty("error")
		JSONRPCError error;
        public JSONRPCResponse(){


		}
		public JSONRPCResponse(String jsonrpc, Object id, Object result, JSONRPCError error) {
			this.jsonrpc = jsonrpc;
			this.id = id;
			this.result = result;
			this.error = error;
		}

		@Override
		public String jsonrpc() {
			return jsonrpc;
		}

		public Object id() {
			return id;
		}

		public Object result() {
			return result;
		}

		public JSONRPCError error() {
			return error;
		}

		@JsonInclude(JsonInclude.Include.NON_ABSENT)
		@JsonIgnoreProperties(ignoreUnknown = true)
		@ToString
		@EqualsAndHashCode
		public static class JSONRPCError {

			@JsonProperty("code")
			int code;

			@JsonProperty("message")
			String message;

			@JsonProperty("data")
			Object data;
            public JSONRPCError() {

			}
			public JSONRPCError(int code, String message, Object data) {
				this.code = code;
				this.message = message;
				this.data = data;
			}

			public int code() {
				return code;
			}

			public String message() {
				return message;
			}

			public Object data() {
				return data;
			}

		}

	}

	// ---------------------------
	// Initialization
	// ---------------------------
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class InitializeRequest implements Request {

		@JsonProperty("protocolVersion")
		String protocolVersion;

		@JsonProperty("capabilities")
		ClientCapabilities capabilities;

		@JsonProperty("clientInfo")
		Implementation clientInfo;
        public InitializeRequest() {

		}
		public InitializeRequest(String protocolVersion, ClientCapabilities capabilities, Implementation clientInfo) {
			this.protocolVersion = protocolVersion;
			this.capabilities = capabilities;
			this.clientInfo = clientInfo;
		}

		public String protocolVersion() {
			return protocolVersion;
		}

		public ClientCapabilities capabilities() {
			return capabilities;
		}

		public Implementation clientInfo() {
			return clientInfo;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class InitializeResult {

		@JsonProperty("protocolVersion")
		String protocolVersion;

		@JsonProperty("capabilities")
		ServerCapabilities capabilities;

		@JsonProperty("serverInfo")
		Implementation serverInfo;

		@JsonProperty("instructions")
		String instructions;

		public InitializeResult() {

		}
		public InitializeResult(String protocolVersion, ServerCapabilities capabilities, Implementation serverInfo,
				String instructions) {
			this.protocolVersion = protocolVersion;
			this.capabilities = capabilities;
			this.serverInfo = serverInfo;
			this.instructions = instructions;
		}

		public String protocolVersion() {
			return protocolVersion;
		}

		public ServerCapabilities capabilities() {
			return capabilities;
		}

		public Implementation serverInfo() {
			return serverInfo;
		}

		public String instructions() {
			return instructions;
		}

	}

	/**
	 * Clients can implement additional features to enrich connected MCP servers with
	 * additional capabilities. These capabilities can be used to extend the functionality
	 * of the server, or to provide additional information to the server about the
	 * client's capabilities. experimental WIP roots define the boundaries of where
	 * servers can operate within the filesystem, allowing them to understand which
	 * directories and files they have access to. sampling Provides a standardized way for
	 * servers to request LLM sampling (“completions” or “generations”) from language
	 * models via clients.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class ClientCapabilities {

		@JsonProperty("experimental")
		Map<String, Object> experimental;

		@JsonProperty("roots")
		RootCapabilities roots;

		@JsonProperty("sampling")
		Sampling sampling;

		public ClientCapabilities() {

		}
		public ClientCapabilities(Map<String, Object> experimental, RootCapabilities roots, Sampling sampling) {
			this.experimental = experimental;
			this.roots = roots;
			this.sampling = sampling;
		}

		public Map<String, Object> experimental() {
			return experimental;
		}

		public RootCapabilities roots() {
			return roots;
		}

		public Sampling sampling() {
			return sampling;
		}

		/**
		 * Roots define the boundaries of where servers can operate within the filesystem,
		 * allowing them to understand which directories and files they have access to.
		 * Servers can request the list of roots from supporting clients and receive
		 * notifications when that list changes. listChanged Whether the client would send
		 * notification about roots has changed since the last time the server checked.
		 */
		@JsonInclude(JsonInclude.Include.NON_ABSENT)
		@JsonIgnoreProperties(ignoreUnknown = true)
		@ToString
		@EqualsAndHashCode
		public static class RootCapabilities {

			@JsonProperty("listChanged")
			Boolean listChanged;
            public RootCapabilities() {

			}
			public RootCapabilities(Boolean listChanged) {
				this.listChanged = listChanged;
			}

			public Boolean listChanged() {
				return listChanged;
			}

		}

		/**
		 * Provides a standardized way for servers to request LLM sampling ("completions"
		 * or "generations") from language models via clients. This flow allows clients to
		 * maintain control over model access, selection, and permissions while enabling
		 * servers to leverage AI capabilities—with no server API keys necessary. Servers
		 * can request text or image-based interactions and optionally include context
		 * from MCP servers in their prompts.
		 */
		@JsonInclude(JsonInclude.Include.NON_ABSENT)
		@ToString
		@EqualsAndHashCode
		public static class Sampling {

			public Sampling() {
			}

		}

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private Map<String, Object> experimental;

			private RootCapabilities roots;

			private Sampling sampling;

			public Builder experimental(Map<String, Object> experimental) {
				this.experimental = experimental;
				return this;
			}

			public Builder roots(Boolean listChanged) {
				this.roots = new RootCapabilities(listChanged);
				return this;
			}

			public Builder sampling() {
				this.sampling = new Sampling();
				return this;
			}

			public ClientCapabilities build() {
				return new ClientCapabilities(experimental, roots, sampling);
			}

		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class ServerCapabilities {

		@JsonProperty("completions")
		CompletionCapabilities completions;

		@JsonProperty("experimental")
		Map<String, Object> experimental;

		@JsonProperty("logging")
		LoggingCapabilities logging;

		@JsonProperty("prompts")
		PromptCapabilities prompts;

		@JsonProperty("resources")
		ResourceCapabilities resources;

		@JsonProperty("tools")
		ToolCapabilities tools;

		public ServerCapabilities() {

		}
		public ServerCapabilities(CompletionCapabilities completions, Map<String, Object> experimental,
				LoggingCapabilities logging, PromptCapabilities prompts, ResourceCapabilities resources,
				ToolCapabilities tools) {
			this.completions = completions;
			this.experimental = experimental;
			this.logging = logging;
			this.prompts = prompts;
			this.resources = resources;
			this.tools = tools;
		}

		public CompletionCapabilities completions() {
			return completions;
		}

		public Map<String, Object> experimental() {
			return experimental;
		}

		public LoggingCapabilities logging() {
			return logging;
		}

		public PromptCapabilities prompts() {
			return prompts;
		}

		public ResourceCapabilities resources() {
			return resources;
		}

		public ToolCapabilities tools() {
			return tools;
		}

		@JsonInclude(JsonInclude.Include.NON_ABSENT)
		@ToString
		@EqualsAndHashCode
		public static class CompletionCapabilities {

			public CompletionCapabilities() {
			}

		}

		@JsonInclude(JsonInclude.Include.NON_ABSENT)
		@ToString
		@EqualsAndHashCode
		public static class LoggingCapabilities {

			public LoggingCapabilities() {
			}

		}

		@JsonInclude(JsonInclude.Include.NON_ABSENT)
		@ToString
		@EqualsAndHashCode
		public static class PromptCapabilities {

			@JsonProperty("listChanged")
			Boolean listChanged;

			public PromptCapabilities(Boolean listChanged) {
				this.listChanged = listChanged;
			}

			public PromptCapabilities() {

			}
			public Boolean listChanged() {
				return listChanged;
			}

		}

		@JsonInclude(JsonInclude.Include.NON_ABSENT)
		@ToString
		@EqualsAndHashCode
		public static class ResourceCapabilities {

			@JsonProperty("subscribe")
			Boolean subscribe;

			@JsonProperty("listChanged")
			Boolean listChanged;

			public ResourceCapabilities(Boolean subscribe, Boolean listChanged) {
				this.subscribe = subscribe;
				this.listChanged = listChanged;
			}
			public ResourceCapabilities() {

			}

			public Boolean subscribe() {
				return subscribe;
			}

			public Boolean listChanged() {
				return listChanged;
			}

		}

		@JsonInclude(JsonInclude.Include.NON_ABSENT)
		@ToString
		@EqualsAndHashCode
		public static class ToolCapabilities {

			@JsonProperty("listChanged")
			Boolean listChanged;

			public ToolCapabilities(Boolean listChanged) {
				this.listChanged = listChanged;
			}
			public ToolCapabilities() {

			}

			public Boolean listChanged() {
				return listChanged;
			}

		}

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private CompletionCapabilities completions;

			private Map<String, Object> experimental;

			private LoggingCapabilities logging = new LoggingCapabilities();

			private PromptCapabilities prompts;

			private ResourceCapabilities resources;

			private ToolCapabilities tools;

			public Builder completions() {
				this.completions = new CompletionCapabilities();
				return this;
			}

			public Builder experimental(Map<String, Object> experimental) {
				this.experimental = experimental;
				return this;
			}

			public Builder logging() {
				this.logging = new LoggingCapabilities();
				return this;
			}

			public Builder prompts(Boolean listChanged) {
				this.prompts = new PromptCapabilities(listChanged);
				return this;
			}

			public Builder resources(Boolean subscribe, Boolean listChanged) {
				this.resources = new ResourceCapabilities(subscribe, listChanged);
				return this;
			}

			public Builder tools(Boolean listChanged) {
				this.tools = new ToolCapabilities(listChanged);
				return this;
			}

			public ServerCapabilities build() {
				return new ServerCapabilities(completions, experimental, logging, prompts, resources, tools);
			}

		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class Implementation {

		@JsonProperty("name")
		String name;

		@JsonProperty("version")
		String version;

		public Implementation() {

		}
		public Implementation(String name, String version) {
			this.name = name;
			this.version = version;
		}

		public String name() {
			return name;
		}

		public String version() {
			return version;
		}

	}

	// Existing Enums and Base Types (from previous implementation)
	public enum Role {

		@JsonProperty("user")
		USER, @JsonProperty("assistant")
		ASSISTANT

	}

	// ---------------------------
	// Resource Interfaces
	// ---------------------------

	/**
	 * Base for objects that include optional annotations for the client. The client can
	 * use annotations to inform how objects are used or displayed
	 */
	public interface Annotated {

		Annotations annotations();

	}

	/**
	 * Optional annotations for the client. The client can use annotations to inform how
	 * objects are used or displayed. audience Describes who the intended customer of this
	 * object or data is. It can include multiple entries to indicate content useful for
	 * multiple audiences (e.g., `["user", "assistant"]`). priority Describes how
	 * important this data is for operating the server. A value of 1 means "most
	 * important," and indicates that the data is effectively required, while 0 means
	 * "least important," and indicates that the data is entirely optional. It is a number
	 * between 0 and 1.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class Annotations {

		@JsonProperty("audience")
		List<Role> audience;

		@JsonProperty("priority")
		Double priority;

		public Annotations() {

		}
		public Annotations(List<Role> audience, Double priority) {
			this.audience = audience;
			this.priority = priority;
		}

		public List<Role> audience() {
			return audience;
		}

		public Double priority() {
			return priority;
		}

	}

	/**
	 * A known resource that the server is capable of reading. uri the URI of the
	 * resource. name A human-readable name for this resource. This can be used by clients
	 * to populate UI elements. description A description of what this resource
	 * represents. This can be used by clients to improve the LLM's understanding of
	 * available resources. It can be thought of like a "hint" to the model. mimeType The
	 * MIME type of this resource, if known. annotations Optional annotations for the
	 * client. The client can use annotations to inform how objects are used or displayed.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class Resource implements Annotated {

		@JsonProperty("uri")
		String uri;

		@JsonProperty("name")
		String name;

		@JsonProperty("description")
		String description;

		@JsonProperty("mimeType")
		String mimeType;

		@JsonProperty("annotations")
		Annotations annotations;

		public Resource() {

		}
		public Resource(String uri, String name, String description, String mimeType, Annotations annotations) {
			this.uri = uri;
			this.name = name;
			this.description = description;
			this.mimeType = mimeType;
			this.annotations = annotations;
		}

		public String uri() {
			return uri;
		}

		public String name() {
			return name;
		}

		public String description() {
			return description;
		}

		public String mimeType() {
			return mimeType;
		}

		@Override
		public Annotations annotations() {
			return annotations;
		}

	}

	/**
	 * Resource templates allow servers to expose parameterized resources using URI
	 * templates. uriTemplate A URI template that can be used to generate URIs for this
	 * resource. name A human-readable name for this resource. This can be used by clients
	 * to populate UI elements. description A description of what this resource
	 * represents. This can be used by clients to improve the LLM's understanding of
	 * available resources. It can be thought of like a "hint" to the model. mimeType The
	 * MIME type of this resource, if known. annotations Optional annotations for the
	 * client. The client can use annotations to inform how objects are used or displayed.
	 *
	 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6570">RFC 6570</a>
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class ResourceTemplate implements Annotated {

		@JsonProperty("uriTemplate")
		String uriTemplate;

		@JsonProperty("name")
		String name;

		@JsonProperty("description")
		String description;

		@JsonProperty("mimeType")
		String mimeType;

		@JsonProperty("annotations")
		Annotations annotations;

		public ResourceTemplate() {

		}
		public ResourceTemplate(String uriTemplate, String name, String description, String mimeType,
				Annotations annotations) {
			this.uriTemplate = uriTemplate;
			this.name = name;
			this.description = description;
			this.mimeType = mimeType;
			this.annotations = annotations;
		}

		public String uriTemplate() {
			return uriTemplate;
		}

		public String name() {
			return name;
		}

		public String description() {
			return description;
		}

		public String mimeType() {
			return mimeType;
		}

		@Override
		public Annotations annotations() {
			return null;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class ListResourcesResult {

		@JsonProperty("resources")
		List<Resource> resources;

		@JsonProperty("nextCursor")
		String nextCursor;

		public ListResourcesResult() {

		}
		public ListResourcesResult(List<Resource> resources, String nextCursor) {
			this.resources = resources;
			this.nextCursor = nextCursor;
		}

		public List<Resource> resources() {
			return resources;
		}

		public String nextCursor() {
			return nextCursor;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class ListResourceTemplatesResult {

		@JsonProperty("resourceTemplates")
		List<ResourceTemplate> resourceTemplates;

		@JsonProperty("nextCursor")
		String nextCursor;

		public ListResourceTemplatesResult() {

		}
		public ListResourceTemplatesResult(List<ResourceTemplate> resourceTemplates, String nextCursor) {
			this.resourceTemplates = resourceTemplates;
			this.nextCursor = nextCursor;
		}

		public List<ResourceTemplate> resourceTemplates() {
			return resourceTemplates;
		}

		public String nextCursor() {
			return nextCursor;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class ReadResourceRequest {

		@JsonProperty("uri")
		String uri;

		public ReadResourceRequest() {

		}
		public ReadResourceRequest(String uri) {
			this.uri = uri;
		}

		public String uri() {
			return uri;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class ReadResourceResult {

		@JsonProperty("contents")
		List<ResourceContents> contents;

		public ReadResourceResult() {

		}
		public ReadResourceResult(List<ResourceContents> contents) {
			this.contents = contents;
		}

		public List<ResourceContents> contents() {
			return contents;
		}

	}

	/**
	 * Sent from the client to request resources/updated notifications from the server
	 * whenever a particular resource changes. uri the URI of the resource to subscribe
	 * to. The URI can use any protocol; it is up to the server how to interpret it.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class SubscribeRequest {

		@JsonProperty("uri")
		String uri;

		public SubscribeRequest(String uri) {
			this.uri = uri;
		}
		public SubscribeRequest() {

		}

		public String uri() {
			return uri;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class UnsubscribeRequest {

		@JsonProperty("uri")
		String uri;

		public UnsubscribeRequest(String uri) {
			this.uri = uri;
		}
		public UnsubscribeRequest() {

		}

		public String uri() {
			return uri;
		}

	}

	/**
	 * The contents of a specific resource or sub-resource.
	 */
	@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, include = As.PROPERTY)
	@JsonSubTypes({ @JsonSubTypes.Type(value = TextResourceContents.class, name = "text"),
			@JsonSubTypes.Type(value = BlobResourceContents.class, name = "blob") })
	public interface ResourceContents {

		/**
		 * The URI of this resource.
		 * @return the URI of this resource.
		 */
		String uri();

		/**
		 * The MIME type of this resource.
		 * @return the MIME type of this resource.
		 */
		String mimeType();

	}

	/**
	 * Text contents of a resource. uri the URI of this resource. mimeType the MIME type
	 * of this resource. text the text of the resource. This must only be set if the
	 * resource can actually be represented as text (not binary data).
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class TextResourceContents implements ResourceContents {

		@JsonProperty("uri")
		String uri;

		@JsonProperty("mimeType")
		String mimeType;

		@JsonProperty("text")
		String text;

		public TextResourceContents(String uri, String mimeType, String text) {
			this.uri = uri;
			this.mimeType = mimeType;
			this.text = text;
		}
		public TextResourceContents() {

		}

		@Override
		public String uri() {
			return uri;
		}

		@Override
		public String mimeType() {
			return mimeType;
		}

		public String text() {
			return text;
		}

	}

	/**
	 * Binary contents of a resource. uri the URI of this resource. mimeType the MIME type
	 * of this resource. blob a base64-encoded string representing the binary data of the
	 * resource. This must only be set if the resource can actually be represented as
	 * binary data (not text).
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class BlobResourceContents implements ResourceContents {

		@JsonProperty("uri")
		String uri;

		@JsonProperty("mimeType")
		String mimeType;

		@JsonProperty("blob")
		String blob;

		public BlobResourceContents(String uri, String mimeType, String blob) {
			this.uri = uri;
			this.mimeType = mimeType;
			this.blob = blob;
		}
		public BlobResourceContents() {

		}

		@Override
		public String uri() {
			return uri;
		}

		@Override
		public String mimeType() {
			return mimeType;
		}

		public String blob() {
			return blob;
		}

	}

	// ---------------------------
	// Prompt Interfaces
	// ---------------------------

	/**
	 * A prompt or prompt template that the server offers. name The name of the prompt or
	 * prompt template. description An optional description of what this prompt provides.
	 * arguments A list of arguments to use for templating the prompt.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class Prompt {

		@JsonProperty("name")
		String name;

		@JsonProperty("description")
		String description;

		@JsonProperty("arguments")
		List<PromptArgument> arguments;

		public Prompt(String name, String description, List<PromptArgument> arguments) {
			this.name = name;
			this.description = description;
			this.arguments = arguments;
		}
		public Prompt() {

		}

		public String name() {
			return name;
		}

		public String description() {
			return description;
		}

		public List<PromptArgument> arguments() {
			return arguments;
		}

	}

	/**
	 * Describes an argument that a prompt can accept. name The name of the argument.
	 * description A human-readable description of the argument. required Whether this
	 * argument must be provided.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class PromptArgument {

		@JsonProperty("name")
		String name;

		@JsonProperty("description")
		String description;

		@JsonProperty("required")
		Boolean required;

		public PromptArgument(String name, String description, Boolean required) {
			this.name = name;
			this.description = description;
			this.required = required;
		}
		public PromptArgument() {

		}

		public String name() {
			return name;
		}

		public String description() {
			return description;
		}

		public Boolean required() {
			return required;
		}

	}

	/**
	 * Describes a message returned as part of a prompt. This is similar to
	 * `SamplingMessage`, but also supports the embedding of resources from the MCP
	 * server. role The sender or recipient of messages and data in a conversation.
	 * content The content of the message of type {@link Content}.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class PromptMessage {

		@JsonProperty("role")
		Role role;

		@JsonProperty("content")
		Content content;

		public PromptMessage(Role role, Content content) {
			this.role = role;
			this.content = content;
		}
		public PromptMessage() {

		}

		public Role role() {
			return role;
		}

		public Content content() {
			return content;
		}

	}

	/**
	 * The server's response to a prompts/list request from the client. prompts A list of
	 * prompts that the server provides. nextCursor An optional cursor for pagination. If
	 * present, indicates there are more prompts available.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class ListPromptsResult {

		@JsonProperty("prompts")
		List<Prompt> prompts;

		@JsonProperty("nextCursor")
		String nextCursor;

		public ListPromptsResult(List<Prompt> prompts, String nextCursor) {
			this.prompts = prompts;
			this.nextCursor = nextCursor;
		}
		public ListPromptsResult() {

		}

		public List<Prompt> prompts() {
			return prompts;
		}

		public String nextCursor() {
			return nextCursor;
		}

	}

	/**
	 * Used by the client to get a prompt provided by the server. name The name of the
	 * prompt or prompt template. arguments Arguments to use for templating the prompt.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class GetPromptRequest implements Request {

		@JsonProperty("name")
		String name;

		@JsonProperty("arguments")
		Map<String, Object> arguments;

		public GetPromptRequest(String name, Map<String, Object> arguments) {
			this.name = name;
			this.arguments = arguments;
		}
		public GetPromptRequest() {

		}

		public String name() {
			return name;
		}

		public Map<String, Object> arguments() {
			return arguments;
		}

	}

	/**
	 * The server's response to a prompts/get request from the client. description An
	 * optional description for the prompt. messages A list of messages to display as part
	 * of the prompt.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class GetPromptResult {

		@JsonProperty("description")
		String description;

		@JsonProperty("messages")
		List<PromptMessage> messages;

		public GetPromptResult(String description, List<PromptMessage> messages) {
			this.description = description;
			this.messages = messages;
		}
		public GetPromptResult() {

		}

		public String description() {
			return description;
		}

		public List<PromptMessage> messages() {
			return messages;
		}

	}

	// ---------------------------
	// Tool Interfaces
	// ---------------------------

	/**
	 * The server's response to a tools/list request from the client. tools A list of
	 * tools that the server provides. nextCursor An optional cursor for pagination. If
	 * present, indicates there are more tools available.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class ListToolsResult {

		@JsonProperty("tools")
		List<Tool> tools;

		@JsonProperty("nextCursor")
		String nextCursor;

		public ListToolsResult(List<Tool> tools, String nextCursor) {
			this.tools = tools;
			this.nextCursor = nextCursor;
		}
		public ListToolsResult() {

		}

		public List<Tool> tools() {
			return tools;
		}

		public String nextCursor() {
			return nextCursor;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class JsonSchema {

		@JsonProperty("type")
		String type;

		@JsonProperty("properties")
		Map<String, Object> properties;

		@JsonProperty("required")
		List<String> required;

		@JsonProperty("additionalProperties")
		Boolean additionalProperties;

		@JsonProperty("$defs")
		Map<String, Object> defs;

		@JsonProperty("definitions")
		Map<String, Object> definitions;

		public JsonSchema() {

		}

		public JsonSchema(String type, Map<String, Object> properties, List<String> required,
				Boolean additionalProperties, Map<String, Object> defs, Map<String, Object> definitions) {
			this.type = type;
			this.properties = properties;
			this.required = required;
			this.additionalProperties = additionalProperties;
			this.defs = defs;
			this.definitions = definitions;
		}

		public String type() {
			return type;
		}

		public Map<String, Object> properties() {
			return properties;
		}

		public List<String> required() {
			return required;
		}

		public Boolean additionalProperties() {
			return additionalProperties;
		}

		public Map<String, Object> defs() {
			return defs;
		}

		public Map<String, Object> definitions() {
			return definitions;
		}

	}

	/**
	 * Represents a tool that the server provides. Tools enable servers to expose
	 * executable functionality to the system. Through these tools, you can interact with
	 * external systems, perform computations, and take actions in the real world. name A
	 * unique identifier for the tool. This name is used when calling the tool.
	 * description A human-readable description of what the tool does. This can be used by
	 * clients to improve the LLM's understanding of available tools. inputSchema A JSON
	 * Schema object that describes the expected structure of the arguments when calling
	 * this tool. This allows clients to validate tool arguments before sending them to
	 * the server.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class Tool {

		@JsonProperty("name")
		String name;

		@JsonProperty("description")
		String description;

		@JsonProperty("inputSchema")
		JsonSchema inputSchema;

		public Tool(String name, String description, JsonSchema inputSchema) {
			this.name = name;
			this.description = description;
			this.inputSchema = inputSchema;
		}

		public Tool(String name, String description, String schema) {
			this(name, description, parseSchema(schema));
		}
		public Tool() {

		}

		public String name() {
			return name;
		}

		public String description() {
			return description;
		}

		public JsonSchema inputSchema() {
			return inputSchema;
		}

	}

	private static JsonSchema parseSchema(String schema) {
		try {
			return OBJECT_MAPPER.readValue(schema, JsonSchema.class);
		}
		catch (IOException e) {
			throw new IllegalArgumentException("Invalid schema: " + schema, e);
		}
	}

	/**
	 * Used by the client to call a tool provided by the server. name The name of the tool
	 * to call. This must match a tool name from tools/list. arguments Arguments to pass
	 * to the tool. These must conform to the tool's input schema.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class CallToolRequest implements Request {

		@JsonProperty("name")
		String name;

		@JsonProperty("arguments")
		Map<String, Object> arguments;

		public CallToolRequest(String name, Map<String, Object> arguments) {
			this.name = name;
			this.arguments = arguments;
		}

		public CallToolRequest(String name, String jsonArguments) {
			this(name, parseJsonArguments(jsonArguments));
		}
		public CallToolRequest() {

		}

		private static Map<String, Object> parseJsonArguments(String jsonArguments) {
			try {
				return OBJECT_MAPPER.readValue(jsonArguments, MAP_TYPE_REF);
			}
			catch (IOException e) {
				throw new IllegalArgumentException("Invalid arguments: " + jsonArguments, e);
			}
		}

		public String name() {
			return name;
		}

		public Map<String, Object> arguments() {
			return arguments;
		}

	}

	/**
	 * The server's response to a tools/call request from the client. content A list of
	 * content items representing the tool's output. Each item can be text, an image, or
	 * an embedded resource. isError If true, indicates that the tool execution failed and
	 * the content contains error information. If false or absent, indicates successful
	 * execution.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class CallToolResult {

		@JsonProperty("content")
		List<Content> content;

		@JsonProperty("isError")
		Boolean isError;

		public CallToolResult(List<Content> content, Boolean isError) {
			this.content = content;
			this.isError = isError;
		}
		public CallToolResult() {

		}

		public List<Content> content() {
			return content;
		}

		public Boolean isError() {
			return isError;
		}

		/**
		 * Creates a new instance of {@link CallToolResult} with a string containing the
		 * tool result.
		 * @param content The content of the tool result. This will be mapped to a
		 * one-sized list with a {@link TextContent} element.
		 * @param isError If true, indicates that the tool execution failed and the
		 * content contains error information. If false or absent, indicates successful
		 * execution.
		 */
		public CallToolResult(String content, Boolean isError) {
			this(Collections.singletonList(new TextContent(content)), isError);
		}

		/**
		 * Creates a builder for {@link CallToolResult}.
		 * @return a new builder instance
		 */
		public static Builder builder() {
			return new Builder();
		}

		/**
		 * Builder for {@link CallToolResult}.
		 */
		public static class Builder {

			private List<Content> content = new ArrayList<>();

			private Boolean isError;

			/**
			 * Sets the content list for the tool result.
			 * @param content the content list
			 * @return this builder
			 */
			public Builder content(List<Content> content) {
				Assert.notNull(content, "content must not be null");
				this.content = content;
				return this;
			}

			/**
			 * Sets the text content for the tool result.
			 * @param textContent the text content
			 * @return this builder
			 */
			public Builder textContent(List<String> textContent) {
				Assert.notNull(textContent, "textContent must not be null");
				textContent.stream().map(TextContent::new).forEach(this.content::add);
				return this;
			}

			/**
			 * Adds a content item to the tool result.
			 * @param contentItem the content item to add
			 * @return this builder
			 */
			public Builder addContent(Content contentItem) {
				Assert.notNull(contentItem, "contentItem must not be null");
				if (this.content == null) {
					this.content = new ArrayList<>();
				}
				this.content.add(contentItem);
				return this;
			}

			/**
			 * Adds a text content item to the tool result.
			 * @param text the text content
			 * @return this builder
			 */
			public Builder addTextContent(String text) {
				Assert.notNull(text, "text must not be null");
				return addContent(new TextContent(text));
			}

			/**
			 * Sets whether the tool execution resulted in an error.
			 * @param isError true if the tool execution failed, false otherwise
			 * @return this builder
			 */
			public Builder isError(Boolean isError) {
				Assert.notNull(isError, "isError must not be null");
				this.isError = isError;
				return this;
			}

			/**
			 * Builds a new {@link CallToolResult} instance.
			 * @return a new CallToolResult instance
			 */
			public CallToolResult build() {
				return new CallToolResult(content, isError);
			}

		}

	}

	// ---------------------------
	// Sampling Interfaces
	// ---------------------------
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class ModelPreferences {

		@JsonProperty("hints")
		List<ModelHint> hints;

		@JsonProperty("costPriority")
		Double costPriority;

		@JsonProperty("speedPriority")
		Double speedPriority;

		@JsonProperty("intelligencePriority")
		Double intelligencePriority;

		public ModelPreferences(List<ModelHint> hints, Double costPriority, Double speedPriority,
				Double intelligencePriority) {
			this.hints = hints;
			this.costPriority = costPriority;
			this.speedPriority = speedPriority;
			this.intelligencePriority = intelligencePriority;
		}
		public ModelPreferences() {

		}

		public List<ModelHint> hints() {
			return hints;
		}

		public Double costPriority() {
			return costPriority;
		}

		public Double speedPriority() {
			return speedPriority;
		}

		public Double intelligencePriority() {
			return intelligencePriority;
		}

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private List<ModelHint> hints;

			private Double costPriority;

			private Double speedPriority;

			private Double intelligencePriority;

			public Builder hints(List<ModelHint> hints) {
				this.hints = hints;
				return this;
			}

			public Builder addHint(String name) {
				if (this.hints == null) {
					this.hints = new ArrayList<>();
				}
				this.hints.add(new ModelHint(name));
				return this;
			}

			public Builder costPriority(Double costPriority) {
				this.costPriority = costPriority;
				return this;
			}

			public Builder speedPriority(Double speedPriority) {
				this.speedPriority = speedPriority;
				return this;
			}

			public Builder intelligencePriority(Double intelligencePriority) {
				this.intelligencePriority = intelligencePriority;
				return this;
			}

			public ModelPreferences build() {
				return new ModelPreferences(hints, costPriority, speedPriority, intelligencePriority);
			}

		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class ModelHint {

		@JsonProperty("name")
		String name;

		public ModelHint(String name) {
			this.name = name;
		}
		public ModelHint() {

		}

		public String name() {
			return name;
		}

		public static ModelHint of(String name) {
			return new ModelHint(name);
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class SamplingMessage {

		@JsonProperty("role")
		Role role;

		@JsonProperty("content")
		Content content;

		public SamplingMessage(Role role, Content content) {
			this.role = role;
			this.content = content;
		}
		public SamplingMessage() {

		}

		public Role role() {
			return role;
		}

		public Content content() {
			return content;
		}

	}

	// Sampling and Message Creation
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class CreateMessageRequest implements Request {

		@JsonProperty("messages")
		List<SamplingMessage> messages;

		@JsonProperty("modelPreferences")
		ModelPreferences modelPreferences;

		@JsonProperty("systemPrompt")
		String systemPrompt;

		@JsonProperty("includeContext")
		ContextInclusionStrategy includeContext;

		@JsonProperty("temperature")
		Double temperature;

		@JsonProperty("maxTokens")
		int maxTokens;

		@JsonProperty("stopSequences")
		List<String> stopSequences;

		@JsonProperty("metadata")
		Map<String, Object> metadata;

		public CreateMessageRequest(List<SamplingMessage> messages, ModelPreferences modelPreferences,
				String systemPrompt, ContextInclusionStrategy includeContext, Double temperature, int maxTokens,
				List<String> stopSequences, Map<String, Object> metadata) {
			this.messages = messages;
			this.modelPreferences = modelPreferences;
			this.systemPrompt = systemPrompt;
			this.includeContext = includeContext;
			this.temperature = temperature;
			this.maxTokens = maxTokens;
			this.stopSequences = stopSequences;
			this.metadata = metadata;
		}
		public CreateMessageRequest() {

		}

		public List<SamplingMessage> messages() {
			return messages;
		}

		public ModelPreferences modelPreferences() {
			return modelPreferences;
		}

		public String systemPrompt() {
			return systemPrompt;
		}

		public ContextInclusionStrategy includeContext() {
			return includeContext;
		}

		public Double temperature() {
			return temperature;
		}

		public int maxTokens() {
			return maxTokens;
		}

		public List<String> stopSequences() {
			return stopSequences;
		}

		public Map<String, Object> metadata() {
			return metadata;
		}

		public enum ContextInclusionStrategy {

			@JsonProperty("none")
			NONE, @JsonProperty("thisServer")
			THIS_SERVER, @JsonProperty("allServers")
			ALL_SERVERS

		}

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private List<SamplingMessage> messages;

			private ModelPreferences modelPreferences;

			private String systemPrompt;

			private ContextInclusionStrategy includeContext;

			private Double temperature;

			private int maxTokens;

			private List<String> stopSequences;

			private Map<String, Object> metadata;

			public Builder messages(List<SamplingMessage> messages) {
				this.messages = messages;
				return this;
			}

			public Builder modelPreferences(ModelPreferences modelPreferences) {
				this.modelPreferences = modelPreferences;
				return this;
			}

			public Builder systemPrompt(String systemPrompt) {
				this.systemPrompt = systemPrompt;
				return this;
			}

			public Builder includeContext(ContextInclusionStrategy includeContext) {
				this.includeContext = includeContext;
				return this;
			}

			public Builder temperature(Double temperature) {
				this.temperature = temperature;
				return this;
			}

			public Builder maxTokens(int maxTokens) {
				this.maxTokens = maxTokens;
				return this;
			}

			public Builder stopSequences(List<String> stopSequences) {
				this.stopSequences = stopSequences;
				return this;
			}

			public Builder metadata(Map<String, Object> metadata) {
				this.metadata = metadata;
				return this;
			}

			public CreateMessageRequest build() {
				return new CreateMessageRequest(messages, modelPreferences, systemPrompt, includeContext, temperature,
						maxTokens, stopSequences, metadata);
			}

		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class CreateMessageResult {

		@JsonProperty("role")
		Role role;

		@JsonProperty("content")
		Content content;

		@JsonProperty("model")
		String model;

		@JsonProperty("stopReason")
		StopReason stopReason;

		public CreateMessageResult(Role role, Content content, String model, StopReason stopReason) {
			this.role = role;
			this.content = content;
			this.model = model;
			this.stopReason = stopReason;
		}
		public CreateMessageResult() {

		}

		public Role role() {
			return role;
		}

		public Content content() {
			return content;
		}

		public String model() {
			return model;
		}

		public StopReason stopReason() {
			return stopReason;
		}

		public enum StopReason {

			@JsonProperty("endTurn")
			END_TURN, @JsonProperty("stopSequence")
			STOP_SEQUENCE, @JsonProperty("maxTokens")
			MAX_TOKENS

		}

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private Role role = Role.ASSISTANT;

			private Content content;

			private String model;

			private StopReason stopReason = StopReason.END_TURN;

			public Builder role(Role role) {
				this.role = role;
				return this;
			}

			public Builder content(Content content) {
				this.content = content;
				return this;
			}

			public Builder model(String model) {
				this.model = model;
				return this;
			}

			public Builder stopReason(StopReason stopReason) {
				this.stopReason = stopReason;
				return this;
			}

			public Builder message(String message) {
				this.content = new TextContent(message);
				return this;
			}

			public CreateMessageResult build() {
				return new CreateMessageResult(role, content, model, stopReason);
			}

		}

	}

	// ---------------------------
	// Pagination Interfaces
	// ---------------------------
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class PaginatedRequest {

		@JsonProperty("cursor")
		String cursor;

		public PaginatedRequest(String cursor) {
			this.cursor = cursor;
		}
		public PaginatedRequest() {

		}

		public String cursor() {
			return cursor;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class PaginatedResult {

		@JsonProperty("nextCursor")
		String nextCursor;

		public PaginatedResult(String nextCursor) {
			this.nextCursor = nextCursor;
		}
		public PaginatedResult() {

		}

		public String nextCursor() {
			return nextCursor;
		}

	}

	// ---------------------------
	// Progress and Logging
	// ---------------------------
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class ProgressNotification {

		@JsonProperty("progressToken")
		String progressToken;

		@JsonProperty("progress")
		double progress;

		@JsonProperty("total")
		Double total;

		public ProgressNotification(String progressToken, double progress, Double total) {
			this.progressToken = progressToken;
			this.progress = progress;
			this.total = total;
		}
		public ProgressNotification() {

		}

		public String progressToken() {
			return progressToken;
		}

		public double progress() {
			return progress;
		}

		public Double total() {
			return total;
		}

	}

	/**
	 * The Model Context Protocol (MCP) provides a standardized way for servers to send
	 * structured log messages to clients. Clients can control logging verbosity by
	 * setting minimum log levels, with servers sending notifications containing severity
	 * levels, optional logger names, and arbitrary JSON-serializable data. level The
	 * severity levels. The minimum log level is set by the client. logger The logger that
	 * generated the message. data JSON-serializable logging data.
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class LoggingMessageNotification {

		@JsonProperty("level")
		LoggingLevel level;

		@JsonProperty("logger")
		String logger;

		@JsonProperty("data")
		String data;

		public LoggingMessageNotification(LoggingLevel level, String logger, String data) {
			this.level = level;
			this.logger = logger;
			this.data = data;
		}
		public LoggingMessageNotification() {

		}

		public LoggingLevel level() {
			return level;
		}

		public String logger() {
			return logger;
		}

		public String data() {
			return data;
		}

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private LoggingLevel level = LoggingLevel.INFO;

			private String logger = "server";

			private String data;

			public Builder level(LoggingLevel level) {
				this.level = level;
				return this;
			}

			public Builder logger(String logger) {
				this.logger = logger;
				return this;
			}

			public Builder data(String data) {
				this.data = data;
				return this;
			}

			public LoggingMessageNotification build() {
				return new LoggingMessageNotification(level, logger, data);
			}

		}

	}

	public enum LoggingLevel {

		@JsonProperty("debug")
		DEBUG(0), @JsonProperty("info")
		INFO(1), @JsonProperty("notice")
		NOTICE(2), @JsonProperty("warning")
		WARNING(3), @JsonProperty("error")
		ERROR(4), @JsonProperty("critical")
		CRITICAL(5), @JsonProperty("alert")
		ALERT(6), @JsonProperty("emergency")
		EMERGENCY(7);

		private final int level;

		LoggingLevel(int level) {
			this.level = level;
		}

		public int level() {
			return level;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class SetLevelRequest {

		@JsonProperty("level")
		LoggingLevel level;

		public SetLevelRequest(LoggingLevel level) {
			this.level = level;
		}
		public SetLevelRequest() {

		}

		public LoggingLevel level() {
			return level;
		}

	}

	// ---------------------------
	// Autocomplete
	// ---------------------------
	public interface CompleteReference {

		String type();

		String identifier();

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class PromptReference implements McpSchema.CompleteReference {

		@JsonProperty("type")
		String type;

		@JsonProperty("name")
		String name;

		public PromptReference(String type, String name) {
			this.type = type;
			this.name = name;
		}

		public PromptReference(String name) {
			this("ref/prompt", name);
		}
		public PromptReference() {

		}

		public String name() {
			return name;
		}

		@Override
		public String type() {
			return type;
		}

		@Override
		public String identifier() {
			return name();
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class ResourceReference implements McpSchema.CompleteReference {

		@JsonProperty("type")
		String type;

		@JsonProperty("uri")
		String uri;

		public ResourceReference(String type, String uri) {
			this.type = type;
			this.uri = uri;
		}

		public ResourceReference(String uri) {
			this("ref/resource", uri);
		}

		public ResourceReference() {

		}

		public String uri() {
			return uri;
		}

		public String type() {
			return type;
		}

		@Override
		public String identifier() {
			return uri();
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class CompleteRequest implements Request {

		@JsonProperty("ref")
		McpSchema.CompleteReference ref;

		@JsonProperty("argument")
		CompleteArgument argument;

		public CompleteRequest(McpSchema.CompleteReference ref, CompleteArgument argument) {
			this.ref = ref;
			this.argument = argument;
		}
		public CompleteRequest() {

		}

		public McpSchema.CompleteReference ref() {
			return ref;
		}

		public CompleteArgument argument() {
			return argument;
		}

		@ToString
		@EqualsAndHashCode
		public static class CompleteArgument {

			@JsonProperty("name")
			String name;

			@JsonProperty("value")
			String value;

			public CompleteArgument(String name, String value) {
				this.name = name;
				this.value = value;
			}
			public CompleteArgument() {

			}

			public String name() {
				return name;
			}

			public String value() {
				return value;
			}

		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class CompleteResult {

		@JsonProperty("completion")
		CompleteCompletion completion;

		public CompleteResult(CompleteCompletion completion) {
			this.completion = completion;
		}
		public CompleteResult() {

		}

		public CompleteCompletion completion() {
			return completion;
		}

		@ToString
		@EqualsAndHashCode
		public static class CompleteCompletion {

			@JsonProperty("values")
			List<String> values;

			@JsonProperty("total")
			Integer total;

			@JsonProperty("hasMore")
			Boolean hasMore;

			public CompleteCompletion(List<String> values, Integer total, Boolean hasMore) {
				this.values = values;
				this.total = total;
				this.hasMore = hasMore;
			}
			public CompleteCompletion() {

			}

			public List<String> values() {
				return values;
			}

			public Integer total() {
				return total;
			}

			public Boolean hasMore() {
				return hasMore;
			}

		}

	}

	// ---------------------------
	// Content Types
	// ---------------------------
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonSubTypes({ @JsonSubTypes.Type(value = TextContent.class, name = "text"),
			@JsonSubTypes.Type(value = ImageContent.class, name = "image"),
			@JsonSubTypes.Type(value = EmbeddedResource.class, name = "resource") })
	public interface Content {

		default String type() {
			if (this instanceof TextContent) {
				return "text";
			}
			else if (this instanceof ImageContent) {
				return "image";
			}
			else if (this instanceof EmbeddedResource) {
				return "resource";
			}
			throw new IllegalArgumentException("Unknown content type: " + this);
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class TextContent implements Content {

		@JsonProperty("audience")
		List<Role> audience;

		@JsonProperty("priority")
		Double priority;

		@JsonProperty("text")
		String text;

		public TextContent(List<Role> audience, Double priority, String text) {
			this.audience = audience;
			this.priority = priority;
			this.text = text;
		}
		public TextContent() {

		}

		public TextContent(String content) {
			this(null, null, content);
		}

		public List<Role> audience() {
			return audience;
		}

		public Double priority() {
			return priority;
		}

		public String text() {
			return text;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class ImageContent implements Content {

		@JsonProperty("audience")
		List<Role> audience;

		@JsonProperty("priority")
		Double priority;

		@JsonProperty("data")
		String data;

		@JsonProperty("mimeType")
		String mimeType;

		public ImageContent(List<Role> audience, Double priority, String data, String mimeType) {
			this.audience = audience;
			this.priority = priority;
			this.data = data;
			this.mimeType = mimeType;
		}
		public ImageContent() {

		}

		public List<Role> audience() {
			return audience;
		}

		public Double priority() {
			return priority;
		}

		public String data() {
			return data;
		}

		public String mimeType() {
			return mimeType;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class EmbeddedResource implements Content {

		@JsonProperty("audience")
		List<Role> audience;

		@JsonProperty("priority")
		Double priority;

		@JsonProperty("resource")
		ResourceContents resource;

		public EmbeddedResource(List<Role> audience, Double priority, ResourceContents resource) {
			this.audience = audience;
			this.priority = priority;
			this.resource = resource;
		}
		public EmbeddedResource() {

		}

		public List<Role> audience() {
			return audience;
		}

		public Double priority() {
			return priority;
		}

		public ResourceContents resource() {
			return resource;
		}

	}

	// ---------------------------
	// Roots
	// ---------------------------

	/**
	 * Represents a root directory or file that the server can operate on. uri The URI
	 * identifying the root. This *must* start with file:// for now. This restriction may
	 * be relaxed in future versions of the protocol to allow other URI schemes. name An
	 * optional name for the root. This can be used to provide a human-readable identifier
	 * for the root, which may be useful for display purposes or for referencing the root
	 * in other parts of the application.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class Root {

		@JsonProperty("uri")
		String uri;

		@JsonProperty("name")
		String name;

		public Root(String uri, String name) {
			this.uri = uri;
			this.name = name;
		}
		public Root() {

		}

		public String uri() {
			return uri;
		}

		public String name() {
			return name;
		}

	}

	/**
	 * The client's response to a roots/list request from the server. This result contains
	 * an array of Root objects, each representing a root directory or file that the
	 * server can operate on. roots An array of Root objects, each representing a root
	 * directory or file that the server can operate on.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	@EqualsAndHashCode
	public static class ListRootsResult {

		@JsonProperty("roots")
		List<Root> roots;
        public ListRootsResult() {

        }
		public ListRootsResult(List<Root> roots) {
			this.roots = roots;
		}

		public List<Root> roots() {
			return roots;
		}

	}

}
