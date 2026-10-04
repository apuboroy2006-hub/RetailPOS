package com.retailpos.repository.mongodb;

import java.time.LocalDateTime;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.retailpos.config.DatabaseConfig;
import com.retailpos.model.OtpCode;
import com.retailpos.repository.OtpRepository;

public class OtpRepositoryImpl implements OtpRepository {

    private final MongoCollection<Document> collection;

    public OtpRepositoryImpl() {

        MongoDatabase database =
                DatabaseConfig.getDatabase();

        this.collection =
                database.getCollection("otp_codes");
    }

    @Override
    public void save(OtpCode otpCode) {

        Document document =
                new Document()
                        .append(
                                "userId",
                                otpCode.getUserId()
                        )
                        .append(
                                "username",
                                otpCode.getUsername()
                        )
                        .append(
                                "deviceId",
                                otpCode.getDeviceId()
                        )
                        .append(
                                "code",
                                otpCode.getCode()
                        )
                        .append(
                                "createdAt",
                                otpCode.getCreatedAt().toString()
                        )
                        .append(
                                "expiresAt",
                                otpCode.getExpiresAt().toString()
                        )
                        .append(
                                "verified",
                                otpCode.isVerified()
                        )
                        .append(
                                "attempts",
                                otpCode.getAttempts()
                        );

        collection.insertOne(document);

        otpCode.setId(
                document
                        .getObjectId("_id")
                        .toString()
        );
    }

    @Override
    public Optional<OtpCode> findLatestByUserIdAndDeviceId(
            String userId,
            String deviceId
    ) {

        Document document =
                collection.find(
                        new Document("userId", userId)
                                .append(
                                        "deviceId",
                                        deviceId
                                )
                )
                .sort(
                        new Document(
                                "createdAt",
                                -1
                        )
                )
                .first();

        if (document == null) {
            return Optional.empty();
        }

        return Optional.of(
                toOtpCode(document)
        );
    }

    @Override
    public void update(OtpCode otpCode) {

        if (!ObjectId.isValid(
                otpCode.getId()
        )) {
            return;
        }

        Document update =
                new Document()
                        .append(
                                "userId",
                                otpCode.getUserId()
                        )
                        .append(
                                "username",
                                otpCode.getUsername()
                        )
                        .append(
                                "deviceId",
                                otpCode.getDeviceId()
                        )
                        .append(
                                "code",
                                otpCode.getCode()
                        )
                        .append(
                                "createdAt",
                                otpCode.getCreatedAt().toString()
                        )
                        .append(
                                "expiresAt",
                                otpCode.getExpiresAt().toString()
                        )
                        .append(
                                "verified",
                                otpCode.isVerified()
                        )
                        .append(
                                "attempts",
                                otpCode.getAttempts()
                        );

        collection.updateOne(
                new Document(
                        "_id",
                        new ObjectId(
                                otpCode.getId()
                        )
                ),
                new Document(
                        "$set",
                        update
                )
        );
    }

    @Override
    public void deleteById(String id) {

        if (!ObjectId.isValid(id)) {
            return;
        }

        collection.deleteOne(
                new Document(
                        "_id",
                        new ObjectId(id)
                )
        );
    }

    private OtpCode toOtpCode(
            Document document
    ) {

        OtpCode otpCode =
                new OtpCode();

        ObjectId objectId =
                document.getObjectId("_id");

        if (objectId != null) {

            otpCode.setId(
                    objectId.toString()
            );
        }

        otpCode.setUserId(
                document.getString("userId")
        );

        otpCode.setUsername(
                document.getString("username")
        );

        otpCode.setDeviceId(
                document.getString("deviceId")
        );

        otpCode.setCode(
                document.getString("code")
        );

        String createdAt =
                document.getString("createdAt");

        if (createdAt != null) {

            otpCode.setCreatedAt(
                    LocalDateTime.parse(
                            createdAt
                    )
            );
        }

        String expiresAt =
                document.getString("expiresAt");

        if (expiresAt != null) {

            otpCode.setExpiresAt(
                    LocalDateTime.parse(
                            expiresAt
                    )
            );
        }

        Boolean verified =
                document.getBoolean("verified");

        otpCode.setVerified(
                verified != null
                        && verified
        );

        Integer attempts =
                document.getInteger("attempts");

        otpCode.setAttempts(
                attempts != null
                        ? attempts
                        : 0
        );

        return otpCode;
    }
}