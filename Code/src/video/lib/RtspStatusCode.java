/**
 * 
 */
package video.lib;

/**
 * @see RFC 2326 [7.1.1]
 * @author yuezhu
 *
 */
public enum RtspStatusCode {

	Continue(100, "Continue"),
	OK(200, "OK"),
	Created(201, "Created"),
	LowOnStorageSpace(250, "Low on Storage Space"),
	MultipleChoices(300, "Multiple Choices"),
	MovedPermanently(301, "Multiple Choices"),
	MovedTemporarily(302, "Moved Temporarily"),
	SeeOther(303, "See Other"),
	NotModified(304, "Not Modified"),
	UseProxy(305, "Use Proxy"),
	BadRequest(400, "Bad Request"),
	Unauthorized(401, "Unauthorized"),
	PaymentRequired(402, "Payment Required"),
	Forbidden(403, "Forbidden"),
	NotFound(404, "Not Found"),
	MethodNotAllowed(405, "Method Not Allowed"),
	NotAcceptable(406, "Not Acceptable"),
	ProxyAuthenticationRequired(407, "Proxy Authentication Required"),
	RequestTimeOut(408, "Request Time-out"),
	Gone(410, "Gone"),
	LengthRequired(411, "Length Required"),
	PreconditionFailed(412, "Precondition Failed"),
	RequestEntityTooLarge(413, "Request Entity Too Large"),
	RequestUriTooLarge(414, "Request-URI Too Large"),
	UnsupportedMediaType(415, "Unsupported Media Type"),
	ParameterNotUnderstood(451, "Parameter Not Understood"),
	ConferenceNotFound(452, "Conference Not Found"),
	NotEnoughBandwidth(453, "Not Enough Bandwidth"),
	SessionNotFound(454, "Session Not Found"),
	MethodNotValidInThisState(455, "Method Not Valid in This State"),
	HeaderFieldNotValidForResource(456, "Header Field Not Valid for Resource"),
	InvalidRange(457, "Invalid Range"),
	ParameterIsReadOnly(458, "Parameter Is Read-Only"),
	AggregateOperationNotAllowed(459, "Aggregate operation not allowed"),
	OnlyAggregateOperationAllowed(460, "Only aggregate operation allowed"),
	UnsupportedTransport(461, "Unsupported transport"),
	DestinationUnreachable(464, "Destination unreachable"),
	InternalServerError(500, "Internal Server Error"), 
	NotImplemented(501, "Not Implemented"),
	BadGateway(502, "Bad Gateway"),
	ServiceUnavailable(503, "Service Unavailable"),
	GatewayTimeOut(504, "Gateway Time-out"),
	RtspVersionNotSupported(505, "RTSP Version not supported"), 
	OptionNotSupported(551, "Option not supported");

	private final int code;
	private final String reason;
	
	private RtspStatusCode(int code, String reason) {
		this.code = code;
		this.reason = reason;
	}
	
	public int toCode() {
		return this.code;
	}
	
	public String toReason() {
		return this.reason;
	}
	
	public static RtspStatusCode fromCode(String code) {
		int codeValue = Integer.parseInt(code);
		for (RtspStatusCode statusCode : RtspStatusCode.values()) {
			if (statusCode.toCode() == codeValue) { 
				return statusCode;
			}
		}
		return RtspStatusCode.BadRequest;
	}
	
	public static RtspStatusCode fromCode(int code) {
		for (RtspStatusCode statusCode : RtspStatusCode.values()) {
			if (statusCode.toCode() == code) { 
				return statusCode;
			}
		}
		return RtspStatusCode.BadRequest;
	}

}
