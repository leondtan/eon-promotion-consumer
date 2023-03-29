package dev.eon.promotionconsumer;

import dev.eon.promotionconsumer.adapter.mail.MailAdapter;
import dev.eon.promotionconsumer.adapter.psql.PsqlAdapter;
import dev.eon.promotionconsumer.model.EligibleUserResult;
import dev.eon.promotionconsumer.model.PromotionalPayload;
import dev.eon.promotionconsumer.util.PropertiesReader;
import dev.eon.promotionconsumer.util.ValueUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainProcess {
    public static void runProcess(String payload, PropertiesReader env, PsqlAdapter database, MailAdapter mail) {
        PromotionalPayload currentPayload = ValueUtil.gson.fromJson(payload, PromotionalPayload.class);
        System.out.println(currentPayload.getTitle());

        try (
                PreparedStatement eligibleUserStatement = database.getConnection().prepareStatement("select " +
                        "? AS g_type, " +
                        "(CASE WHEN ? = 'year' THEN EXTRACT(YEAR FROM bt.created_at) " +
                        "ELSE EXTRACT(MONTH FROM bt.created_at) " +
                        "END) AS g_col, " +
                        "SUM(bt.credit * bt.credit_rate) AS g_credit, " +
                        "SUM(bt.debit * bt.debit_rate) AS g_debit, " +
                        "u.email AS g_email, " +
                        "u.name AS g_name " +
                        "FROM " +
                        "balance_transactions bt JOIN users u ON bt.user_id = u.id " +
                        "WHERE (CASE WHEN ? = 'year' THEN EXTRACT(YEAR FROM bt.created_at) = ? " +
                        "ELSE EXTRACT(MONTH FROM bt.created_at) = ? " +
                        "END) " +
                        "GROUP BY " +
                        "g_col, " +
                        "u.email, " +
                        "u.name " +
                        "HAVING (SUM(debit * debit_rate) - SUM(credit * credit_rate)) > ?");

        ) {
            eligibleUserStatement.setString(1, currentPayload.getPeriodType());
            eligibleUserStatement.setString(2, currentPayload.getPeriodType());
            eligibleUserStatement.setString(3, currentPayload.getPeriodType());
            eligibleUserStatement.setInt(4, currentPayload.getPeriodValue());
            eligibleUserStatement.setInt(5, currentPayload.getPeriodValue());
            eligibleUserStatement.setDouble(6, currentPayload.getBalanceThreshold());

            ResultSet resultSet = eligibleUserStatement.executeQuery();

            List<EligibleUserResult> eligibleUsers = new ArrayList<>();
            while (resultSet.next()) {
                eligibleUsers.add(new EligibleUserResult(
                        resultSet.getString("g_email"),
                        resultSet.getString("g_name"),
                        resultSet.getString("g_type"),
                        resultSet.getInt("g_col"),
                        resultSet.getDouble("g_debit"),
                        resultSet.getDouble("g_credit")
                ));
            }
            System.out.printf("Result: " + ValueUtil.gson.toJson(eligibleUsers) + "\n");

            eligibleUsers.forEach((user) -> {
                String tempPeriod = "";
                if (!currentPayload.getPeriodType().equals("year")) {
                    String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                    tempPeriod = "month " + months[currentPayload.getPeriodValue() - 1];
                } else {
                    tempPeriod = "year " + currentPayload.getPeriodValue();
                }
                mail.sendPromotionEmail(
                        user.getEmail(),
                        user.getName(),
                        currentPayload.getTitle(),
                        "Congratulation, you are chosen for our promotion " + currentPayload.getTitle() + " for period " + tempPeriod
                );
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
