package com.techtorque.notification_service.grpc;

import com.techtorque.notification.grpc.NotificationEmailServiceGrpc;
import com.techtorque.notification.grpc.SendEmailRequest;
import com.techtorque.notification.grpc.SendEmailResponse;
import com.techtorque.notification_service.service.TransactionalEmailService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Exposes transactional email delivery over gRPC for other services.
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class NotificationEmailGrpcService extends NotificationEmailServiceGrpc.NotificationEmailServiceImplBase {

    private final TransactionalEmailService transactionalEmailService;

    @Override
    public void sendTransactionalEmail(SendEmailRequest request, StreamObserver<SendEmailResponse> responseObserver) {
        try {
            log.debug("Received transactional email request for {} using template {}", request.getTo(), request.getType());
            var result = transactionalEmailService.sendTransactionalEmail(
                    request.getTo(),
                    request.getUsername(),
                    request.getType(),
                    request.getVariablesMap());

            SendEmailResponse response = SendEmailResponse.newBuilder()
                    .setStatus(result.status())
                    .setMessageId(result.messageId())
                    .setDetail(result.detail() == null ? "" : result.detail())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Unexpected error while handling transactional email request: {}", ex.getMessage(), ex);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to process transactional email request")
                    .withCause(ex)
                    .asRuntimeException());
        }
    }
}
