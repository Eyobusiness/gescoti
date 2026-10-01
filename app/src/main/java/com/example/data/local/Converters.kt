package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.*

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole?): String? = value?.name
    @TypeConverter
    fun toUserRole(value: String?): UserRole? = value?.let { enumValueOf<UserRole>(it) }

    @TypeConverter
    fun fromMemberStatus(value: MemberStatus?): String? = value?.name
    @TypeConverter
    fun toMemberStatus(value: String?): MemberStatus? = value?.let { enumValueOf<MemberStatus>(it) }

    @TypeConverter
    fun fromMemberType(value: MemberType?): String? = value?.name
    @TypeConverter
    fun toMemberType(value: String?): MemberType? = value?.let { enumValueOf<MemberType>(it) }

    @TypeConverter
    fun fromDuesStatus(value: DuesStatus?): String? = value?.name
    @TypeConverter
    fun toDuesStatus(value: String?): DuesStatus? = value?.let { enumValueOf<DuesStatus>(it) }

    @TypeConverter
    fun fromPaymentMode(value: PaymentMode?): String? = value?.name
    @TypeConverter
    fun toPaymentMode(value: String?): PaymentMode? = value?.let { enumValueOf<PaymentMode>(it) }

    @TypeConverter
    fun fromCashMovementType(value: CashMovementType?): String? = value?.name
    @TypeConverter
    fun toCashMovementType(value: String?): CashMovementType? = value?.let { enumValueOf<CashMovementType>(it) }

    @TypeConverter
    fun fromCashMovementSource(value: CashMovementSource?): String? = value?.name
    @TypeConverter
    fun toCashMovementSource(value: String?): CashMovementSource? = value?.let { enumValueOf<CashMovementSource>(it) }

    @TypeConverter
    fun fromCampaignStatus(value: CampaignStatus?): String? = value?.name
    @TypeConverter
    fun toCampaignStatus(value: String?): CampaignStatus? = value?.let { enumValueOf<CampaignStatus>(it) }

    @TypeConverter
    fun fromActivityType(value: ActivityType?): String? = value?.name
    @TypeConverter
    fun toActivityType(value: String?): ActivityType? = value?.let { enumValueOf<ActivityType>(it) }

    @TypeConverter
    fun fromActivityStatus(value: ActivityStatus?): String? = value?.name
    @TypeConverter
    fun toActivityStatus(value: String?): ActivityStatus? = value?.let { enumValueOf<ActivityStatus>(it) }

    @TypeConverter
    fun fromAuditAction(value: AuditAction?): String? = value?.name
    @TypeConverter
    fun toAuditAction(value: String?): AuditAction? = value?.let { enumValueOf<AuditAction>(it) }

    @TypeConverter
    fun fromNotificationType(value: NotificationType?): String? = value?.name
    @TypeConverter
    fun toNotificationType(value: String?): NotificationType? = value?.let { enumValueOf<NotificationType>(it) }
}
