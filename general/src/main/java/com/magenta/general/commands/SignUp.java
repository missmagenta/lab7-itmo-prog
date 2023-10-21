package com.magenta.general.commands;

import com.magenta.general.dto.CommandResultDto;
import com.magenta.general.userdata.User;
import com.magenta.general.utils.HistoryManagable;
import com.magenta.general.utils.UserDataManagable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

public class SignUp extends UserAbstractCommand {
    private String[] loginAndPassword;

    public SignUp() {
        super("sign_up", "зарегистрировать пользователя");
    }

    public SignUp(String[] loginAndPassword) {
        this();
        this.loginAndPassword = loginAndPassword;
    }

    @Override
    public CommandResultDto execute(UserDataManagable userDataManager, HistoryManagable historyManager) {
        historyManager.addNote(this.getName());
        if (userDataManager.isUniqueUsername(loginAndPassword[0])) {
            userDataManager.add(new User(-1L, loginAndPassword[0], loginAndPassword[1]));
        } else {
            return new SignUp.RegisterCommandResult(false);
        }
        return new SignUp.RegisterCommandResult(true);
    }

    @EqualsAndHashCode
    @ToString
    public static class RegisterCommandResult extends CommandResultDto {
        private final boolean wasRegistered;

        public boolean isWasRegistered() {
            return wasRegistered;
        }

        public RegisterCommandResult(boolean wasRegistered) {
            super(wasRegistered ? "New user registered." : "Username is not unique. Use another username.", true);
            this.wasRegistered = wasRegistered;
        }
    }
}
