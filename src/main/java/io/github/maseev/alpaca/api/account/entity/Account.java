package io.github.maseev.alpaca.api.account.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.maseev.alpaca.api.AlpacaAPI;
import io.github.maseev.alpaca.api.util.Available;
import io.github.maseev.alpaca.http.json.util.DateFormatUtil;
import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value.Immutable
@JsonSerialize(as = ImmutableAccount.class)
@JsonDeserialize(as = ImmutableAccount.class)
public interface Account {

  enum Status {
    /**
     * The account is onboarding
     */
    ONBOARDING,

    /**
     * The account application submission failed for some reason
     */
    SUBMISSION_FAILED,

    /**
     * The account application has been submitted for review
     */
    SUBMITTED,

    /**
     * The account information is being updated
     */
    ACCOUNT_UPDATED,

    /**
     * The final account approval is pending
     */
    APPROVAL_PENDING,

    /**
     * The account is active for trading
     */
    ACTIVE,

    /**
     * The account application has been rejected
     */
    REJECTED
  }

  /**
   * @return Account ID
   */
  String id();

  /**
   * @return Account Number
   */
  @Available(in = Available.Version.V2)
  @JsonProperty("account_number")
  String accountNumber();

  Status status();

  String currency();

  /**
   * @return current available $ buying power; If multiplier = 4, this is your daytrade buying power
   * which is calculated as (last_equity - (last) maintenance_margin) * 4; If multiplier = 2,
   * buying_power = max(equity – initial_margin,0) * 2; If multiplier = 1, buying_power = cash
   */
  @JsonProperty("buying_power")
  BigDecimal buyingPower();

  /**
   * @return Your buying power under Regulation T is calculated as below :
   * (your excess equity - equity minus margin value - times your margin multiplier)
   */
  @Available(in = Available.Version.V2)
  @JsonProperty("regt_buying_power")
  BigDecimal regtBuyingPower();

  /**
   * @return Your buying power for day trades (continuously updated value)
   */
  @Available(in = Available.Version.V2)
  @JsonProperty("daytrading_buying_power")
  BigDecimal dayTradingBuyingPower();

  /**
   * @return Cash balance
   */
  BigDecimal cash();

  /**
   * @return Withdrawable cash amount (this parameter might be empty in {@link AlpacaAPI.Version#V2 V2})
   */
  @Nullable
  @Available(in = Available.Version.V1)
  @JsonProperty("cash_withdrawable")
  BigDecimal cashWithdrawable();

  /**
   * @return Total value of cash + holding positions
   */
  @JsonProperty("portfolio_value")
  BigDecimal portfolioValue();

  /**
   * @return Whether or not the account has been flagged as a pattern day trader
   */
  @JsonProperty("pattern_day_trader")
  boolean patternDayTrader();

  /**
   * @return Whether or not the account is allowed to place orders
   */
  @JsonProperty("trading_blocked")
  boolean tradingBlocked();

  /**
   * @return Whether or not the account is allowed to request money transfers
   */
  @JsonProperty("transfers_blocked")
  boolean transfersBlocked();

  /**
   * @return If true, the account activity by user is prohibited
   */
  @JsonProperty("account_blocked")
  boolean accountBlocked();

  @JsonProperty("trade_suspended_by_user")
  boolean tradeSuspendedByUser();

  /**
   * @return Timestamap this account was created at
   */
  @JsonProperty("created_at")
  @JsonFormat(pattern = DateFormatUtil.DATE_TIME_FORMAT)
  LocalDateTime createdAt();

  /**
   * @return Flag to denote whether or not the account is permitted to short
   */
  @Nullable
  @Available(in = Available.Version.V2)
  @JsonProperty("shorting_enabled")
  Boolean shortingEnabled();

  /**
   * @return Buying power multiplier that represents account margin classification; valid values 1
   * (standard limited margin account with 1x buying power), 2 (reg T margin account with 2x
   * intraday and overnight buying power; this is the default for all non-PDT accounts with $2,000
   * or more equity), 4 (PDT account with 4x intraday buying power and 2x reg T overnight buying
   * power)
   */
  @Nullable
  @Available(in = Available.Version.V2)
  Integer multiplier();

  /**
   * @return Real-time MtM value of all long positions held in the account
   */
  @Nullable
  @Available(in = Available.Version.V2)
  @JsonProperty("long_market_value")
  BigDecimal longMarketValue();

  /**
   * @return Real-time MtM value of all short positions held in the account
   */
  @Nullable
  @Available(in = Available.Version.V2)
  @JsonProperty("short_market_value")
  BigDecimal shortMarketValue();

  /**
   * @return Cash + long_market_value + short_market_value
   */
  @Nullable
  @Available(in = Available.Version.V2)
  BigDecimal equity();

  /**
   * @return Equity as of previous trading day at 16:00:00 ET
   */
  @Nullable
  @Available(in = Available.Version.V2)
  @JsonProperty("last_equity")
  BigDecimal lastEquity();

  /**
   * @return Reg T initial margin requirement (continuously updated value)
   */
  @Nullable
  @Available(in = Available.Version.V2)
  @JsonProperty("initial_margin")
  BigDecimal initialMargin();

  /**
   * @return Maintenance margin requirement (continuously updated value)
   */
  @Nullable
  @Available(in = Available.Version.V2)
  @JsonProperty("maintenance_margin")
  BigDecimal maintenanceMargin();

  /**
   * @return Maintenance margin requirement on the previous trading day
   */
  @Nullable
  @Available(in = Available.Version.V2)
  @JsonProperty("last_maintenance_margin")
  BigDecimal lastMaintenanceMargin();

  /**
   * @return the current number of daytrades that have been made in the last 5 trading days
   * (inclusive of today)
   */
  @Nullable
  @Available(in = Available.Version.V2)
  @JsonProperty("daytrade_count")
  BigDecimal daytradeCount();

  /**
   * @return value of special memorandum account (will be used at a later date to provide additional
   * buying_power)
   */
  @Nullable
  @Available(in = Available.Version.V2)
  BigDecimal sma();
}
